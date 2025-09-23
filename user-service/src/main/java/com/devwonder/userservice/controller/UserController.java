package com.devwonder.userservice.controller;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.userservice.dto.CheckCustomerExistsResponse;
import com.devwonder.userservice.dto.DealerRequest;
import com.devwonder.userservice.dto.DealerResponse;
import com.devwonder.userservice.dto.DealerUpdateRequest;
import com.devwonder.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "Users & Dealers", description = "👥 User & dealer management - Public registration & Admin operations")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/dealers")
    @Operation(
        summary = "Get All Dealers",
        description = "Retrieve a list of all dealers (business partners) in the system. " +
                    "This endpoint provides dealer company information including contact details and location.",
        security = {}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dealers retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<DealerResponse>>> getAllDealers() {
        List<DealerResponse> dealers = userService.getAllDealers();
        BaseResponse<List<DealerResponse>> response = new BaseResponse<>(
            true,
            "Dealers retrieved successfully",
            dealers
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dealers/{id}")
    @Operation(
        summary = "Get Dealer by ID (ADMIN Only)",
        description = "Retrieve detailed information of a specific dealer by ID with optional field filtering. " +
                    "This endpoint provides complete dealer profile including company information, contact details and location. " +
                    "Use 'fields' parameter to specify which fields to return (e.g., ?fields=companyName,email,phone). " +
                    "Available fields: accountId, companyName, address, phone, email, district, city. " +
                    "Requires ADMIN role authorization.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dealer retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "404", description = "Dealer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<DealerResponse>> getDealerById(
            @PathVariable Long id,
            @RequestParam(required = false) String fields) {
        DealerResponse dealer = userService.getDealerById(id, fields);
        BaseResponse<DealerResponse> response = new BaseResponse<>(
            true,
            "Dealer retrieved successfully",
            dealer
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/dealers")
    @Operation(
        summary = "Register New Dealer",
        description = "Register a new dealer (business partner) in the system. " +
                    "This endpoint creates dealer profile with company information, contact details and location. " +
                    "The accountId must correspond to an existing account in the auth service with DEALER role.",
        security = {}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Dealer registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Dealer already exists for this account"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<DealerResponse>> registerDealer(@Valid @RequestBody DealerRequest dealerRequest) {
        DealerResponse dealerResponse = userService.createDealer(dealerRequest);
        BaseResponse<DealerResponse> response = new BaseResponse<>(
            true,
            "Dealer registered successfully",
            dealerResponse
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/dealers/{id}")
    @Operation(
        summary = "Update Dealer Information (ADMIN Only)",
        description = "Update existing dealer information. This endpoint allows admin users to modify dealer " +
                    "company information, contact details, and location. Requires ADMIN role authorization.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dealer updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Dealer not found"),
        @ApiResponse(responseCode = "409", description = "Phone or email already exists for another dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<DealerResponse>> updateDealer(
            @PathVariable Long id,
            @Valid @RequestBody DealerUpdateRequest updateRequest) {
        DealerResponse dealerResponse = userService.updateDealer(id, updateRequest);
        BaseResponse<DealerResponse> response = new BaseResponse<>(
            true,
            "Dealer updated successfully",
            dealerResponse
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/dealers/{id}")
    @Operation(
        summary = "Delete Dealer (ADMIN Only)",
        description = "Delete an existing dealer from the system. This endpoint permanently removes the dealer " +
                    "record from the database. Requires ADMIN role authorization. Use with caution as this operation " +
                    "cannot be undone.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dealer deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Dealer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<String>> deleteDealer(@PathVariable Long id) {
        userService.deleteDealer(id);
        BaseResponse<String> response = new BaseResponse<>(
            true,
            "Dealer deleted successfully",
            "Dealer with ID " + id + " has been permanently removed"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{identifier}/check-exists")
    @Operation(
        summary = "Check if customer exists",
        description = "Check if customer exists by phone or email. Returns detailed customer information if found.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check completed successfully")
    })
    public ResponseEntity<BaseResponse<CheckCustomerExistsResponse>> checkCustomerExists(
            @PathVariable String identifier) {

        log.info("Checking customer existence: {}", identifier);

        CheckCustomerExistsResponse response = userService.checkCustomerExistsByIdentifier(identifier);
        return ResponseEntity.ok(BaseResponse.success("Customer check completed", response));
    }
}