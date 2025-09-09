package com.devwonder.userservice.controller;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.userservice.dto.DealerResponse;
import com.devwonder.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}