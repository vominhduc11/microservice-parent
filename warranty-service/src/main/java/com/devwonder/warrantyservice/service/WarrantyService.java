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

    public WarrantyBulkCreateResponse createWarranties(WarrantyCreateRequest request) {
        log.info("Creating warranties for product: {} with {} serials",
                request.getProductId(), request.getSerialNumbers().size());

        // 1. Handle customer (create new or find existing)
        Long customerId = handleCustomer(request.getCustomer());

        // 2. Get product warranty period
        Integer warrantyPeriod = getProductWarrantyPeriod(request.getProductId());

        // 3. Create warranties for each serial
        List<WarrantyResponse> successfulWarranties = new ArrayList<>();
        List<String> failedSerials = new ArrayList<>();

        for (String serial : request.getSerialNumbers()) {
            try {
                WarrantyResponse warranty = createSingleWarranty(
                    serial,
                    customerId,
                    warrantyPeriod,
                    request.getPurchaseDate()
                );
                successfulWarranties.add(warranty);
            } catch (Exception e) {
                log.error("Failed to create warranty for serial {}: {}", serial, e.getMessage());
                failedSerials.add(serial);
            }
        }

        // 4. Get customer name for response
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

            var response = userServiceClient.checkCustomerExists(customerWrapper.getCustomerIdentifier());
            if (!response.isSuccess() || response.getData() == null) {
                throw new CustomerOperationException("Customer not found: " + customerWrapper.getCustomerIdentifier());
            }
            return response.getData();
        } else {
            // Create new customer
            log.info("Creating new customer: {}", customerWrapper.getCustomerInfo().getName());

            var response = userServiceClient.createCustomer(customerWrapper.getCustomerInfo());
            if (!response.isSuccess() || response.getData() == null) {
                throw new CustomerOperationException("Failed to create customer: " + response.getMessage());
            }
            return response.getData();
        }
    }

    private Integer getProductWarrantyPeriod(Long productId) {
        var response = productServiceClient.getProductWarrantyPeriod(productId);
        if (!response.isSuccess() || response.getData() == null) {
            log.warn("Could not get warranty period for product {}, using default 12 months", productId);
            return 12; // Default warranty period
        }
        return response.getData();
    }

    private WarrantyResponse createSingleWarranty(String serial, Long customerId, Integer warrantyPeriod, java.time.LocalDate purchaseDate) {
        // Get product serial ID
        var serialResponse = productServiceClient.getProductSerialIdBySerial(serial);
        if (!serialResponse.isSuccess() || serialResponse.getData() == null) {
            throw new ResourceNotFoundException("Product serial not found: " + serial);
        }

        Long productSerialId = serialResponse.getData();

        // Check if warranty already exists
        if (warrantyRepository.findActiveWarrantyByProductSerial(productSerialId).isPresent()) {
            throw new WarrantyAlreadyExistsException("Active warranty already exists for serial: " + serial);
        }

        // Generate warranty code
        String warrantyCode = generateWarrantyCode(serial);

        // Calculate dates
        LocalDateTime startDate = purchaseDate.atStartOfDay();
        LocalDateTime endDate = startDate.plusMonths(warrantyPeriod);

        // Create warranty
        Warranty warranty = Warranty.builder()
                .idProductSerial(productSerialId)
                .idCustomer(customerId)
                .warrantyCode(warrantyCode)
                .warrantyPeriod(warrantyPeriod)
                .startDate(startDate)
                .endDate(endDate)
                .status(WarrantyStatus.ACTIVE)
                .purchaseDate(startDate)
                .build();

        Warranty savedWarranty = warrantyRepository.save(warranty);
        log.info("Warranty created for serial {} with code {}", serial, warrantyCode);

        return mapToResponse(savedWarranty);
    }

    private String generateWarrantyCode(String serial) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return String.format("WR-%s-%s-%s", timestamp, serial, randomSuffix);
    }

    private String getCustomerName(Long customerId) {
        try {
            var response = userServiceClient.getCustomerName(customerId);
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
        return warranty.getStatus() == WarrantyStatus.ACTIVE &&
               warranty.getEndDate().isAfter(LocalDateTime.now());
    }

    public boolean isWarrantyExpired(Warranty warranty) {
        return warranty.getEndDate().isBefore(LocalDateTime.now()) ||
               warranty.getStatus() == WarrantyStatus.EXPIRED;
    }

    private WarrantyResponse mapToResponse(Warranty warranty) {
        return WarrantyResponse.builder()
                .id(warranty.getId())
                .idProductSerial(warranty.getIdProductSerial())
                .idCustomer(warranty.getIdCustomer())
                .warrantyCode(warranty.getWarrantyCode())
                .warrantyPeriod(warranty.getWarrantyPeriod())
                .startDate(warranty.getStartDate())
                .endDate(warranty.getEndDate())
                .status(warranty.getStatus())
                .purchaseDate(warranty.getPurchaseDate())
                .createAt(warranty.getCreateAt())
                .build();
    }
}