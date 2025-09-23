package com.devwonder.userservice.service;

import com.devwonder.userservice.client.AuthServiceClient;
import com.devwonder.userservice.dto.AuthAccountCreateRequest;
import com.devwonder.userservice.dto.CheckCustomerExistsResponse;
import com.devwonder.userservice.dto.CustomerInfo;
import com.devwonder.userservice.entity.Customer;
import com.devwonder.userservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuthServiceClient authServiceClient;
    private final CustomerEventService customerEventService;

    @Value("${auth.api.key:user-service-key}")
    private String authApiKey;

    // Email regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // Phone regex pattern (supports +, digits, spaces, dashes, parentheses)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9\\s\\-\\(\\)]{10,15}$"
    );

    // Password generation
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    @Transactional(readOnly = true)
    public CheckCustomerExistsResponse checkCustomerExistsByIdentifier(String identifier) {
        log.info("Checking customer existence with identifier: {}", identifier);

        if (identifier == null || identifier.trim().isEmpty()) {
            return CheckCustomerExistsResponse.builder()
                .exists(false)
                .message("Identifier cannot be empty")
                .build();
        }

        String trimmedIdentifier = identifier.trim();

        // Determine if identifier is email or phone
        if (EMAIL_PATTERN.matcher(trimmedIdentifier).matches()) {
            // Check by email
            Optional<Customer> customerByEmail = customerRepository.findByEmail(trimmedIdentifier);
            if (customerByEmail.isPresent()) {
                Customer customer = customerByEmail.get();
                log.info("Customer found by email: {} with accountId: {}",
                    trimmedIdentifier, customer.getAccountId());

                return CheckCustomerExistsResponse.builder()
                    .exists(true)
                    .matchedBy("email")
                    .message("Customer account found by email")
                    .customerInfo(buildCustomerInfo(customer))
                    .build();
            }
        } else if (PHONE_PATTERN.matcher(trimmedIdentifier).matches()) {
            // Check by phone
            Optional<Customer> customerByPhone = customerRepository.findByPhone(trimmedIdentifier);
            if (customerByPhone.isPresent()) {
                Customer customer = customerByPhone.get();
                log.info("Customer found by phone: {} with accountId: {}",
                    trimmedIdentifier, customer.getAccountId());

                return CheckCustomerExistsResponse.builder()
                    .exists(true)
                    .matchedBy("phone")
                    .message("Customer account found by phone number")
                    .customerInfo(buildCustomerInfo(customer))
                    .build();
            }
        } else {
            // Invalid format
            return CheckCustomerExistsResponse.builder()
                .exists(false)
                .message("Invalid identifier format. Must be a valid email or phone number")
                .build();
        }

        // No customer found
        log.info("No customer found with identifier: {}", trimmedIdentifier);

        return CheckCustomerExistsResponse.builder()
            .exists(false)
            .message("No customer account found with the provided identifier")
            .build();
    }

    @Transactional
    public Long createCustomer(CustomerInfo customerInfo) {
        log.info("Creating new customer: {}", customerInfo.getName());

        // Validate required fields
        if (customerInfo.getName() == null || customerInfo.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (customerInfo.getPhone() == null || customerInfo.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer phone is required");
        }

        // Check if customer already exists in user-service
        if (customerRepository.findByPhone(customerInfo.getPhone()).isPresent()) {
            throw new RuntimeException("Customer with phone " + customerInfo.getPhone() + " already exists");
        }
        if (customerInfo.getEmail() != null && !customerInfo.getEmail().trim().isEmpty() &&
            customerRepository.findByEmail(customerInfo.getEmail()).isPresent()) {
            throw new RuntimeException("Customer with email " + customerInfo.getEmail() + " already exists");
        }

        // Cross-service validation: Check if username already exists in auth-service
        String username = customerInfo.getPhone().trim();
        if (checkUsernameExistsInAuthService(username)) {
            throw new RuntimeException("Account with username " + username + " already exists");
        }

        // 1. Create account in auth-service first
        String tempPassword = generateTempPassword();
        Long realAccountId = createAccountInAuthService(customerInfo, username, tempPassword);

        try {
            // 2. Create customer with real accountId
            Customer customer = Customer.builder()
                    .accountId(realAccountId)
                    .name(customerInfo.getName().trim())
                    .email(customerInfo.getEmail() != null ? customerInfo.getEmail().trim() : null)
                    .phone(customerInfo.getPhone().trim())
                    .address(customerInfo.getAddress())
                    .build();

            Customer savedCustomer = customerRepository.save(customer);
            log.info("Customer created successfully with real accountId: {}", savedCustomer.getAccountId());

            // 3. Publish Kafka event for email notification
            customerEventService.publishCustomerCreatedEvent(savedCustomer, username, tempPassword);

            return savedCustomer.getAccountId();
        } catch (Exception e) {
            // If customer creation fails, delete the created account
            log.error("Failed to create customer, rolling back account creation: {}", e.getMessage());
            try {
                authServiceClient.deleteAccount(realAccountId, authApiKey);
                log.info("Successfully rolled back account creation for accountId: {}", realAccountId);
            } catch (Exception rollbackException) {
                log.error("Failed to rollback account creation for accountId {}: {}", realAccountId, rollbackException.getMessage());
            }
            throw new RuntimeException("Failed to create customer: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Long findCustomerIdByIdentifier(String identifier) {
        log.info("Finding customer ID by identifier: {}", identifier);

        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }

        String trimmedIdentifier = identifier.trim();

        // Check by email
        if (EMAIL_PATTERN.matcher(trimmedIdentifier).matches()) {
            Optional<Customer> customer = customerRepository.findByEmail(trimmedIdentifier);
            return customer.map(Customer::getAccountId).orElse(null);
        }

        // Check by phone
        if (PHONE_PATTERN.matcher(trimmedIdentifier).matches()) {
            Optional<Customer> customer = customerRepository.findByPhone(trimmedIdentifier);
            return customer.map(Customer::getAccountId).orElse(null);
        }

        return null;
    }

    @Transactional(readOnly = true)
    public String getCustomerName(Long customerId) {
        log.info("Getting customer name for ID: {}", customerId);

        Optional<Customer> customer = customerRepository.findByAccountId(customerId);
        return customer.map(Customer::getName).orElse(null);
    }

    @Transactional(readOnly = true)
    public CustomerInfo getCustomerDetails(Long customerId) {
        log.info("Getting customer details for ID: {}", customerId);

        Optional<Customer> customer = customerRepository.findByAccountId(customerId);
        return customer.map(this::buildCustomerInfo).orElse(null);
    }

    private Long createAccountInAuthService(CustomerInfo customerInfo, String username, String tempPassword) {
        log.info("Creating account in auth-service for customer: {}", customerInfo.getName());

        // Create account request
        AuthAccountCreateRequest authRequest = AuthAccountCreateRequest.builder()
                .username(username)
                .password(tempPassword)
                .roleNames(Set.of("CUSTOMER"))
                .build();

        try {
            var authResponse = authServiceClient.createAccount(authRequest, authApiKey);

            if (!authResponse.isSuccess() || authResponse.getData() == null) {
                throw new RuntimeException("Failed to create account in auth-service: " + authResponse.getMessage());
            }

            Long accountId = authResponse.getData().getId();
            log.info("Successfully created account in auth-service with ID: {} for customer: {}", accountId, customerInfo.getName());

            return accountId;
        } catch (Exception e) {
            log.error("Error creating account in auth-service for customer {}: {}", customerInfo.getName(), e.getMessage());
            throw new RuntimeException("Failed to create customer account: " + e.getMessage(), e);
        }
    }

    private boolean checkUsernameExistsInAuthService(String username) {
        log.info("Checking if username exists in auth-service: {}", username);

        try {
            var response = authServiceClient.checkUsernameExists(username, authApiKey);

            if (!response.isSuccess()) {
                log.error("Failed to check username existence in auth-service: {}", response.getMessage());
                throw new RuntimeException("Failed to validate username: " + response.getMessage());
            }

            boolean exists = response.getData();
            log.info("Username {} exists in auth-service: {}", username, exists);

            return exists;
        } catch (Exception e) {
            log.error("Error checking username existence in auth-service for {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to validate username: " + e.getMessage(), e);
        }
    }

    private String generateTempPassword() {
        // Generate 8-character temporary password
        StringBuilder password = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }

    private CustomerInfo buildCustomerInfo(Customer customer) {
        return CustomerInfo.builder()
            .accountId(customer.getAccountId())
            .name(customer.getName())
            .email(customer.getEmail())
            .phone(customer.getPhone())
            .address(customer.getAddress())
            .build();
    }
}