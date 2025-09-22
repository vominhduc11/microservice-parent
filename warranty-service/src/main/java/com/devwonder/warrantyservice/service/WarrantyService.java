package com.devwonder.warrantyservice.service;

import com.devwonder.common.exception.ResourceNotFoundException;
import com.devwonder.warrantyservice.client.ProductServiceClient;
import com.devwonder.warrantyservice.client.UserServiceClient;
import com.devwonder.warrantyservice.dto.*;
import com.devwonder.warrantyservice.entity.Warranty;
import com.devwonder.warrantyservice.enums.WarrantyStatus;
import com.devwonder.warrantyservice.exception.CustomerOperationException;
import com.devwonder.warrantyservice.exception.WarrantyAlreadyExistsException;
import com.devwonder.warrantyservice.exception.WarrantyNotFoundException;
import com.devwonder.warrantyservice.repository.WarrantyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WarrantyService {

    private final WarrantyRepository warrantyRepository;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;

    @Value("${auth.api.key:INTER_SERVICE_KEY}")
    private String authApiKey;

    public WarrantyBulkCreateResponse createWarranties(WarrantyCreateRequest request) {
        log.info("Creating warranties for product: {} with {} serials",
                request.getProductId(), request.getSerialNumbers().size());

        // 1. Handle customer (create new or find existing)
        Long customerId = handleCustomer(request.getCustomer());

        // 2. Create warranties for each serial
        List<WarrantyResponse> successfulWarranties = new ArrayList<>();
        List<String> failedSerials = new ArrayList<>();
        List<String> successfulSerials = new ArrayList<>();

        for (String serial : request.getSerialNumbers()) {
            try {
                WarrantyResponse warranty = createSingleWarranty(
                    serial,
                    customerId,
                    request.getPurchaseDate()
                );
                successfulWarranties.add(warranty);
                successfulSerials.add(serial);
                log.info("Successfully created warranty for serial: {} with code: {}",
                         serial, warranty.getWarrantyCode());
            } catch (WarrantyAlreadyExistsException e) {
                log.warn("Warranty already exists for serial {}: {}", serial, e.getMessage());
                failedSerials.add(serial);
            } catch (ResourceNotFoundException e) {
                log.warn("Product serial not found {}: {}", serial, e.getMessage());
                failedSerials.add(serial);
            } catch (Exception e) {
                log.error("Unexpected error creating warranty for serial {}: {}", serial, e.getMessage(), e);
                failedSerials.add(serial);
            }
        }

        // 4. Update product serials to SOLD_TO_CUSTOMER status
        if (!successfulSerials.isEmpty()) {
            updateProductSerialsStatus(successfulSerials);
        }

        // 5. Get customer name for response
        String customerName = getCustomerName(customerId);

        return WarrantyBulkCreateResponse.builder()
                .customerId(customerId)
                .customerName(customerName)
                .warranties(successfulWarranties)
                .totalWarranties(successfulWarranties.size())
                .failedSerials(failedSerials)
                .build();
    }

    private Long handleCustomer(CustomerWrapper customerWrapper) {
        if (customerWrapper.getCustomerExists()) {
            // Find existing customer
            log.info("Looking up existing customer: {}", customerWrapper.getCustomerIdentifier());

            var response = userServiceClient.checkCustomerExists(customerWrapper.getCustomerIdentifier(), authApiKey);
            if (!response.isSuccess() || response.getData() == null || !response.getData().isExists()) {
                throw new CustomerOperationException("Customer not found: " + customerWrapper.getCustomerIdentifier());
            }

            // Get accountId from the customer info returned by user service
            Long accountId = response.getData().getCustomerInfo().getAccountId();
            if (accountId == null) {
                throw new CustomerOperationException("Customer found but accountId is missing");
            }
            log.info("Found existing customer with accountId: {}", accountId);
            return accountId;
        } else {
            // Create new customer with fallback to existing lookup
            log.info("Creating new customer: {}", customerWrapper.getCustomerInfo().getName());

            try {
                var response = userServiceClient.createCustomer(customerWrapper.getCustomerInfo(), authApiKey);
                if (!response.isSuccess() || response.getData() == null) {
                    throw new CustomerOperationException("Failed to create customer: " + response.getMessage());
                }
                log.info("Successfully created new customer with accountId: {}", response.getData());
                return response.getData();
            } catch (Exception e) {
                // Check if error is due to customer already existing
                if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                    log.info("Customer already exists, attempting fallback lookup by phone: {}",
                             customerWrapper.getCustomerInfo().getPhone());

                    // Fallback: Try to find existing customer by phone
                    try {
                        var existingResponse = userServiceClient.checkCustomerExists(
                            customerWrapper.getCustomerInfo().getPhone(), authApiKey);

                        if (existingResponse.isSuccess() && existingResponse.getData() != null &&
                            existingResponse.getData().isExists()) {

                            Long accountId = existingResponse.getData().getCustomerInfo().getAccountId();
                            if (accountId != null) {
                                log.info("Fallback successful: Found existing customer with accountId: {}", accountId);
                                return accountId;
                            }
                        }
                    } catch (Exception fallbackException) {
                        log.error("Fallback lookup also failed: {}", fallbackException.getMessage());
                    }
                }

                // If fallback failed or error is not about existing customer, re-throw original error
                log.error("Customer creation failed and no fallback available: {}", e.getMessage());
                throw new CustomerOperationException("Failed to create or find customer: " + e.getMessage());
            }
        }
    }


    private WarrantyResponse createSingleWarranty(String serial, Long customerId, java.time.LocalDate purchaseDate) {
        log.debug("Creating warranty for serial: {}, customer: {}, purchaseDate: {}", serial, customerId, purchaseDate);

        // Get product serial ID
        log.debug("Calling product service with API key: {}", authApiKey != null ? "***" + authApiKey.substring(Math.max(0, authApiKey.length() - 4)) : "null");
        var serialResponse = productServiceClient.getProductSerialIdBySerial(serial, authApiKey);
        if (!serialResponse.isSuccess() || serialResponse.getData() == null) {
            log.error("Failed to get product serial ID for serial: {}, response: {}, API key used: {}",
                     serial, serialResponse.getMessage(), authApiKey != null ? "***" + authApiKey.substring(Math.max(0, authApiKey.length() - 4)) : "null");
            throw new ResourceNotFoundException("Product serial not found: " + serial);
        }

        Long productSerialId = serialResponse.getData();
        log.debug("Retrieved product serial ID: {} for serial: {}", productSerialId, serial);

        // Check if warranty already exists
        if (warrantyRepository.findActiveWarrantyByProductSerial(productSerialId).isPresent()) {
            log.warn("Active warranty already exists for product serial ID: {}, serial: {}", productSerialId, serial);
            throw new WarrantyAlreadyExistsException("Active warranty already exists for serial: " + serial);
        }

        // Generate warranty code
        String warrantyCode = generateWarrantyCode(serial);
        log.debug("Generated warranty code: {} for serial: {}", warrantyCode, serial);

        // Create warranty entity
        Warranty warranty = Warranty.builder()
                .idProductSerial(productSerialId)
                .idCustomer(customerId)
                .warrantyCode(warrantyCode)
                .status(WarrantyStatus.ACTIVE)
                .purchaseDate(purchaseDate.atStartOfDay())
                .build();

        try {
            Warranty savedWarranty = warrantyRepository.save(warranty);
            log.info("Successfully saved warranty to database - ID: {}, Code: {}, Serial: {}",
                     savedWarranty.getId(), warrantyCode, serial);

            return mapToResponse(savedWarranty);
        } catch (Exception e) {
            log.error("Failed to save warranty to database for serial: {}, error: {}", serial, e.getMessage(), e);
            throw new RuntimeException("Failed to save warranty: " + e.getMessage(), e);
        }
    }

    private String generateWarrantyCode(String serial) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return String.format("WR-%s-%s-%s", timestamp, serial, randomSuffix);
    }

    private String getCustomerName(Long customerId) {
        try {
            var response = userServiceClient.getCustomerName(customerId, authApiKey);
            return response.isSuccess() ? response.getData() : "Unknown Customer";
        } catch (Exception e) {
            log.warn("Could not get customer name for ID {}: {}", customerId, e.getMessage());
            return "Unknown Customer";
        }
    }

    @Transactional(readOnly = true)
    public List<WarrantyResponse> getWarrantiesByCustomer(Long customerId) {
        return warrantyRepository.findByIdCustomer(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public WarrantyResponse getWarrantyByProductSerial(Long productSerialId) {
        log.info("Checking warranty for product serial: {}", productSerialId);

        Warranty warranty = warrantyRepository.findActiveWarrantyByProductSerial(productSerialId)
                .orElseThrow(() -> new WarrantyNotFoundException("No active warranty found for product serial: " + productSerialId));

        return mapToResponse(warranty);
    }

    public boolean isWarrantyActive(Warranty warranty) {
        LocalDateTime endDate = warranty.getPurchaseDate().plusMonths(24); // Calculate end date: purchase + 24 months
        return warranty.getStatus() == WarrantyStatus.ACTIVE &&
               endDate.isAfter(LocalDateTime.now());
    }

    public boolean isWarrantyExpired(Warranty warranty) {
        LocalDateTime endDate = warranty.getPurchaseDate().plusMonths(24); // Calculate end date: purchase + 24 months
        return endDate.isBefore(LocalDateTime.now()) ||
               warranty.getStatus() == WarrantyStatus.EXPIRED;
    }

    private void updateProductSerialsStatus(List<String> serialNumbers) {
        try {
            ProductSerialBulkStatusUpdateRequest request = ProductSerialBulkStatusUpdateRequest.builder()
                    .serialNumbers(serialNumbers)
                    .status("SOLD_TO_CUSTOMER")
                    .build();

            var response = productServiceClient.updateProductSerialsToSoldToCustomer(request, authApiKey);

            if (response.isSuccess()) {
                log.info("Successfully updated {} product serials to SOLD_TO_CUSTOMER status", serialNumbers.size());
            } else {
                log.error("Failed to update product serials status: {}", response.getMessage());
            }
        } catch (Exception e) {
            log.error("Error updating product serials status: {}", e.getMessage());
        }
    }

    private WarrantyResponse mapToResponse(Warranty warranty) {
        return WarrantyResponse.builder()
                .id(warranty.getId())
                .idProductSerial(warranty.getIdProductSerial())
                .idCustomer(warranty.getIdCustomer())
                .warrantyCode(warranty.getWarrantyCode())
                .status(warranty.getStatus())
                .purchaseDate(warranty.getPurchaseDate())
                .createAt(warranty.getCreateAt())
                .build();
    }
}