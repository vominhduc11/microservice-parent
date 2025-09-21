package com.devwonder.userservice.service;

import com.devwonder.userservice.dto.CustomerInfo;
import com.devwonder.userservice.entity.Customer;
import com.devwonder.userservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    // Email regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // Phone regex pattern (supports +, digits, spaces, dashes, parentheses)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9\\s\\-\\(\\)]{10,15}$"
    );


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

        // Check if customer already exists
        if (customerRepository.findByPhone(customerInfo.getPhone()).isPresent()) {
            throw new RuntimeException("Customer with phone " + customerInfo.getPhone() + " already exists");
        }
        if (customerInfo.getEmail() != null && !customerInfo.getEmail().trim().isEmpty() &&
            customerRepository.findByEmail(customerInfo.getEmail()).isPresent()) {
            throw new RuntimeException("Customer with email " + customerInfo.getEmail() + " already exists");
        }

        // Generate accountId (simple auto-increment approach)
        Long accountId = generateAccountId();

        // Create customer
        Customer customer = Customer.builder()
                .accountId(accountId)
                .name(customerInfo.getName().trim())
                .email(customerInfo.getEmail() != null ? customerInfo.getEmail().trim() : null)
                .phone(customerInfo.getPhone().trim())
                .address(customerInfo.getAddress())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully with accountId: {}", savedCustomer.getAccountId());

        return savedCustomer.getAccountId();
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

    private Long generateAccountId() {
        // Simple approach: find max accountId and increment
        // In production, consider using sequences or UUID
        Long maxAccountId = customerRepository.findMaxAccountId();
        return maxAccountId != null ? maxAccountId + 1 : 1L;
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