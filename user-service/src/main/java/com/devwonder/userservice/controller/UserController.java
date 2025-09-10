package com.devwonder.userservice.controller;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.userservice.dto.DealerRequest;
import com.devwonder.userservice.dto.DealerResponse;
import com.devwonder.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "User Management", description = "User management endpoints for customers, dealers, and admins")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/dealers")
    @Operation(
        summary = "Get All Dealers",
        description = "Retrieve a list of all dealers (business partners) in the system. " +
                    "This endpoint provides dealer company information including contact details and location."
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

    @PostMapping("/dealers")
    @Operation(
        summary = "Register New Dealer",
        description = "Register a new dealer (business partner) in the system. " +
                    "This endpoint creates dealer profile with company information, contact details and location. " +
                    "The accountId must correspond to an existing account in the auth service with DEALER role."
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
}