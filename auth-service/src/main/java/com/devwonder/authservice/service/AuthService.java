package com.devwonder.authservice.service;

import com.devwonder.authservice.dto.*;
import com.devwonder.authservice.entity.Account;
import com.devwonder.authservice.entity.Role;
import com.devwonder.authservice.repository.AccountRepository;
import com.devwonder.authservice.repository.RoleRepository;
import com.devwonder.common.exception.AuthenticationException;
import com.devwonder.common.exception.ResourceAlreadyExistsException;
import com.devwonder.common.exception.TokenExpiredException;
import com.devwonder.common.exception.TokenBlacklistedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional(readOnly = true)
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        // Find account by username
        Account account = accountRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));


        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), account.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        // Check if account is still active (not soft deleted)
        if (account.getDeleteAt() != null) {
            throw new AuthenticationException("Account is disabled");
        }

        // Extract roles
        log.info("Account {} has {} roles: {}", account.getUsername(),
                account.getRoles().size(),
                account.getRoles().stream().map(Role::getName).toList());
        
        Set<String> roles = account.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        // Create JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("userId", account.getId());

        // Generate access token (30 minutes)
        String accessToken = jwtService.generateToken(account.getUsername(), claims);
        
        // Generate refresh token (7 days)
        String refreshToken = jwtService.generateRefreshToken(account.getUsername(), claims);

        return new LoginResponse(
            accessToken,
            refreshToken,
            jwtService.getAccessTokenExpirationInSeconds(),
            jwtService.getRefreshTokenExpirationInSeconds(),
            account.getUsername(),
            roles
        );
    }

    @Transactional
    public LogoutResponse logoutUser(HttpServletRequest request) {
        try {
            // Extract token from Authorization header
            String token = extractTokenFromRequest(request);
            
            if (!StringUtils.hasText(token)) {
                throw new AuthenticationException("No authorization token provided");
            }
            
            // Validate token format and extract username
            String username = jwtService.extractUsername(token);
            
            // Check if token is already expired
            if (jwtService.isTokenExpired(token)) {
                log.warn("Attempt to logout with expired token for user: {}", username);
                throw new TokenExpiredException("Token is already expired");
            }
            
            // Check if token is already blacklisted
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                log.warn("Attempt to logout with already blacklisted token for user: {}", username);
                throw new TokenBlacklistedException("Token is already invalid");
            }
            
            // Add token to blacklist
            tokenBlacklistService.blacklistToken(token);
            
            log.info("User {} logged out successfully", username);
            
            return new LogoutResponse(
                "Successfully logged out", 
                Instant.now().toString()
            );
            
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            throw new AuthenticationException("Logout failed: " + e.getMessage());
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }

    @Transactional(readOnly = true)
    public RefreshTokenResponse refreshToken(RefreshTokenRequest refreshRequest) {
        String refreshToken = refreshRequest.getToken();
        
        // Extract username from refresh token
        String username = jwtService.extractUsername(refreshToken);
        
        // Check if refresh token is blacklisted
        if (tokenBlacklistService.isTokenBlacklisted(refreshToken)) {
            throw new TokenBlacklistedException("Refresh token has been invalidated");
        }
        
        // Validate refresh token specifically (must be valid and not expired)
        if (!jwtService.isRefreshTokenValid(refreshToken, username)) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }
        
        // Get user account to refresh roles and data
        Account account = accountRepository.findByUsername(username)
            .orElseThrow(() -> new AuthenticationException("User account not found"));
        
        // Check if account is still active (not soft deleted)
        if (account.getDeleteAt() != null) {
            throw new AuthenticationException("Account is disabled");
        }
        
        // Extract current roles
        Set<String> roles = account.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
        
        // Create new JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("userId", account.getId());
        
        // Generate new access token (30 minutes)
        String newAccessToken = jwtService.generateToken(username, claims);
        
        log.info("Token refreshed successfully for user: {}", username);
        
        return new RefreshTokenResponse(
            newAccessToken,
            jwtService.getAccessTokenExpirationInSeconds(),
            username,
            roles,
            Instant.now().toString()
        );
    }

    @Transactional
    public AccountCreateResponse createAccount(AccountCreateRequest request) {
        log.info("Creating new account with username: {}", request.getUsername());
        
        // Check if username already exists
        if (accountRepository.existsByUsername(request.getUsername())) {
            log.warn("Account with username {} already exists", request.getUsername());
            throw new ResourceAlreadyExistsException("Account with username " + request.getUsername() + " already exists");
        }
        
        // Get roles from database
        Set<Role> roles = request.getRoleNames().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toSet());
        
        // Create new account
        Account account = Account.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();
        
        // Save account
        Account savedAccount = accountRepository.save(account);
        log.info("Successfully created account with ID: {} and username: {}", savedAccount.getId(), savedAccount.getUsername());
        
        // Convert roles to role names
        Set<String> roleNames = savedAccount.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        
        // Return response
        return AccountCreateResponse.builder()
                .id(savedAccount.getId())
                .username(savedAccount.getUsername())
                .roles(roleNames)
                .build();
    }
}