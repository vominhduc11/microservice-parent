package com.devwonder.userservice.controller;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.userservice.dto.CheckCustomerExistsResponse;
import com.devwonder.userservice.dto.CustomerInfo;
import com.devwonder.userservice.service.CustomerService;
import com.devwonder.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
@Tag(name = "Customers (Gateway)", description = "👥 Customer operations via API Gateway")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create new customer via Gateway",
               description = "Creates a new customer record via API Gateway. Requires DEALER role authentication.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid customer data"),
            @ApiResponse(responseCode = "409", description = "Customer already exists")
    })
    public ResponseEntity<BaseResponse<Long>> createCustomer(
            @Valid @RequestBody CustomerInfo customerInfo) {

        log.info("Creating customer: {}", customerInfo.getName());

        try {
            Long customerId = customerService.createCustomer(customerInfo);
            return ResponseEntity.status(201)
                    .body(BaseResponse.success("Customer created successfully", customerId));
        } catch (IllegalArgumentException e) {
            log.error("Invalid customer data: {}", e.getMessage());
            return ResponseEntity.status(400)
                    .body(BaseResponse.error("Invalid customer data: " + e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Customer creation failed: {}", e.getMessage());
            return ResponseEntity.status(409)
                    .body(BaseResponse.error(e.getMessage()));
        }
    }


    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer name via Gateway",
               description = "Get customer name by customer ID via API Gateway. Requires authentication.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer name retrieved"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<BaseResponse<String>> getCustomerName(
            @PathVariable Long customerId) {

        log.info("Getting customer name for ID: {}", customerId);

        String customerName = customerService.getCustomerName(customerId);
        if (customerName != null) {
            return ResponseEntity.ok(BaseResponse.success("Customer name retrieved", customerName));
        } else {
            return ResponseEntity.status(404)
                    .body(BaseResponse.error("Customer not found"));
        }
    }

    @GetMapping("/{customerId}/details")
    @Operation(summary = "Get customer details",
               description = "Get detailed customer information by customer ID. Supports both Gateway and inter-service access.",
               security = {@SecurityRequirement(name = "apiKey"), @SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer details retrieved"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<BaseResponse<CustomerInfo>> getCustomerDetails(
            @PathVariable Long customerId) {

        log.info("Getting customer details for ID: {}", customerId);

        CustomerInfo customerDetails = customerService.getCustomerDetails(customerId);
        if (customerDetails != null) {
            return ResponseEntity.ok(BaseResponse.success("Customer details retrieved", customerDetails));
        } else {
            return ResponseEntity.status(404)
                    .body(BaseResponse.error("Customer not found"));
        }
    }

}