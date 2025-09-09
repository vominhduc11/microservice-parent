package com.devwonder.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login response payload")
public class LoginResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Token expiration time in seconds", example = "86400")
    private Long expiresIn;

    @Schema(description = "Authenticated username", example = "admin")
    private String username;

    @Schema(description = "User roles", example = "[\"ADMIN\", \"USER\"]")
    private Set<String> roles;

    public LoginResponse(String accessToken, Long expiresIn, String username, Set<String> roles) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.username = username;
        this.roles = roles;
    }
}