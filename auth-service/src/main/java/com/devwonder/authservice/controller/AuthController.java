package com.devwonder.authservice.controller;

import com.devwonder.authservice.dto.*;
import com.devwonder.authservice.service.AuthService;
import com.devwonder.common.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication endpoints for user login and token management")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "User Login",
        description = "Authenticate user credentials and return JWT access token. " +
                    "Optional userType field can be provided to validate against specific user role. " +
                    "The token can be used for accessing protected resources in other microservices.",
        security = {}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload"),
        @ApiResponse(responseCode = "401", description = "Invalid username or password"),
        @ApiResponse(responseCode = "403", description = "Account is disabled"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(BaseResponse.success("Login successful", response));
    }

    @PostMapping("/logout")
    @Operation(
        summary = "User Logout",
        description = "Invalidate JWT token from Authorization header and add it to blacklist. " +
                    "The token will no longer be valid for accessing protected resources. " +
                    "No request body required - token is automatically extracted from Authorization header."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful, token invalidated"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token"),
        @ApiResponse(responseCode = "403", description = "No authorization header provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<LogoutResponse>> logout(HttpServletRequest request) {
        LogoutResponse response = authService.logoutUser(request);
        return ResponseEntity.ok(BaseResponse.success("Logout successful", response));
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh JWT Token",
        description = "Refresh an expired or soon-to-expire JWT token and get a new access token. " +
                    "The old token must have valid signature but can be expired. " +
                    "After refresh, the old token becomes invalid and should not be used.",
        security = {}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully, new token returned"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload or token format"),
        @ApiResponse(responseCode = "401", description = "Invalid token signature or user not found"),
        @ApiResponse(responseCode = "403", description = "Token is blacklisted or account is disabled"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<RefreshTokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        RefreshTokenResponse response = authService.refreshToken(refreshRequest);
        return ResponseEntity.ok(BaseResponse.success("Token refreshed successfully", response));
    }


    @GetMapping("/validate")
    @Operation(
        summary = "Validate JWT Token", 
        description = "Validate JWT token from Authorization header. " +
                    "Returns token status and user information if valid."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token"),
        @ApiResponse(responseCode = "403", description = "No Authorization header provided")
    })
    public ResponseEntity<BaseResponse<String>> validateToken(
            @Parameter(hidden = true) HttpServletRequest request) {
        boolean isValid = authService.validateTokenFromHeader(request);
        return ResponseEntity.ok(BaseResponse.success(
            isValid ? "Token is valid" : "Token is invalid",
            isValid ? "VALID" : "INVALID"
        ));
    }

}