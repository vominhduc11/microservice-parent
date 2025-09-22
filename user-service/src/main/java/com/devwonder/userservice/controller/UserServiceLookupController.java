package com.devwonder.userservice.controller;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.userservice.dto.CheckCustomerExistsResponse;
import com.devwonder.userservice.dto.CustomerInfo;
import com.devwonder.userservice.dto.DealerResponse;
import com.devwonder.userservice.service.CustomerService;
import com.devwonder.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-service")
@Tag(name = "User Service Lookup", description = "User service lookup endpoints for inter-service communication")
@RequiredArgsConstructor
@Slf4j
public class UserServiceLookupController {

    private final UserService userService;
    private final CustomerService customerService;

    @GetMapping("/customers/{identifier}/check-exists")
    @Operation(
        summary = "Check if customer exists",
        description = "Check if customer exists by phone or email. Used by warranty service. Requires API key authentication.",
        security = @SecurityRequirement(name = "apiKey")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing API key"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<CheckCustomerExistsResponse>> checkCustomerExists(
            @PathVariable String identifier) {

        log.info("Inter-service call: Checking customer existence: {}", identifier);

        CheckCustomerExistsResponse response = userService.checkCustomerExistsByIdentifier(identifier);
        return ResponseEntity.ok(BaseResponse.success("Customer check completed", response));
    }

    @PostMapping("/customers")
    @Operation(
        summary = "Create new customer",
        description = "Creates a new customer record. Used by warranty service for new customer creation. Requires API key authentication.",
        security = @SecurityRequirement(name = "apiKey")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Customer created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid customer data"),
        @ApiResponse(responseCode = "409", description = "Customer already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing API key"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<Long>> createCustomer(
            @Valid @RequestBody CustomerInfo customerInfo) {

        log.info("Inter-service call: Creating customer: {}", customerInfo.getName());

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

    @GetMapping("/customers/{customerId}")
    @Operation(
        summary = "Get customer name",
        description = "Get customer name by customer ID. Used by warranty service. Requires API key authentication.",
        security = @SecurityRequirement(name = "apiKey")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer name retrieved"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing API key"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<String>> getCustomerName(
            @PathVariable Long customerId) {

        log.info("Inter-service call: Getting customer name for ID: {}", customerId);

        String customerName = customerService.getCustomerName(customerId);
        if (customerName != null) {
            return ResponseEntity.ok(BaseResponse.success("Customer name retrieved", customerName));
        } else {
            return ResponseEntity.status(404)
                    .body(BaseResponse.error("Customer not found"));
        }
    }

    @GetMapping("/dealers/{dealerId}")
    @Operation(
        summary = "Get Dealer Information",
        description = "Retrieve dealer information by ID with optional field filtering. Used by order service. Requires API key authentication.",
        security = @SecurityRequirement(name = "apiKey")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dealer retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Dealer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing API key"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<DealerResponse>> getDealerInfo(
            @PathVariable Long dealerId,
            @RequestParam(required = false) String fields) {

        log.info("Inter-service call: Getting dealer info for ID: {} with fields: {}", dealerId, fields);

        DealerResponse dealer = userService.getDealerById(dealerId, fields);
        return ResponseEntity.ok(BaseResponse.success("Dealer retrieved successfully", dealer));
    }
}