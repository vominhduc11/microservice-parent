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
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
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
    private final AuthJwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthEventService authEventService;
    private final com.devwonder.authservice.client.UserServiceClient userServiceClient;

    @org.springframework.beans.factory.annotation.Value("${auth.api.key:INTER_SERVICE_KEY}")
    private String authApiKey;

    @Transactional(readOnly = true)
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        Account account = accountRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), account.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }
        log.info("Account {} has {} roles: {}", account.getUsername(),
                account.getRoles().size(),
                account.getRoles().stream().map(Role::getName).toList());

        Set<String> roles = account.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        // Validate userType if provided
        if (StringUtils.hasText(loginRequest.getUserType())) {
            String requestedUserType = loginRequest.getUserType().toUpperCase();
            if (!roles.contains(requestedUserType)) {
                throw new AuthenticationException("User type '" + requestedUserType + "' does not match account roles");
            }
            log.info("Login validation successful for user {} with userType: {}", account.getUsername(), requestedUserType);
        }

        // Create JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("userId", account.getId());

        // Generate access token (30 minutes)
        String accessToken = jwtService.generateToken(account.getUsername(), claims);

        // Generate refresh token (7 days)
        String refreshToken = jwtService.generateRefreshToken(account.getUsername(), claims);

        // Get email and requireLoginEmailConfirmation setting only for ADMIN
        String email = null;
        Boolean requireLoginEmailConfirmation = null;  // null cho non-ADMIN
        if (roles.contains("ADMIN")) {
            try {
                email = userServiceClient
                    .getAdminEmail(account.getId(), authApiKey)
                    .getBody()
                    .getData();

                requireLoginEmailConfirmation = userServiceClient
                    .getAdminRequireLoginEmailConfirmation(account.getId(), authApiKey)
                    .getBody()
                    .getData();
            } catch (Exception e) {
                log.warn("Failed to fetch admin info for accountId {}: {}", account.getId(), e.getMessage());
                requireLoginEmailConfirmation = false;  // Fallback to false nếu lỗi
            }
        }

        return new LoginResponse(
            accessToken,
            refreshToken,
            jwtService.getAccessTokenExpirationInSeconds(),
            jwtService.getRefreshTokenExpirationInSeconds(),
            account.getUsername(),
            roles,
            account.getId(),
            email,
            requireLoginEmailConfirmation
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
        
        
        Set<String> roles = account.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("userId", account.getId());
        
        // Generate new access token (5 seconds)
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

    @Transactional(readOnly = true)
    public boolean checkUsernameExists(String username) {
        log.info("Checking if username exists: {}", username);

        boolean exists = accountRepository.existsByUsername(username);
        log.info("Username {} exists: {}", username, exists);

        return exists;
    }

    @Transactional
    public void deleteAccount(Long accountId) {
        log.info("Deleting account with ID: {}", accountId);

        // Check if account exists
        if (!accountRepository.existsById(accountId)) {
            log.warn("Account not found with ID: {}", accountId);
            throw new RuntimeException("Account not found with ID: " + accountId);
        }

        // Delete account (hard delete)
        accountRepository.deleteById(accountId);
        log.info("Successfully deleted account with ID: {}", accountId);
    }

    @Transactional(readOnly = true)
    public java.util.List<Long> getAccountIdsByRoleExcluding(String roleName, String excludeRoleName) {
        log.info("Fetching account IDs with role {} excluding {}", roleName, excludeRoleName);

        java.util.List<Account> accounts = accountRepository.findAccountsByRoleNameExcluding(roleName, excludeRoleName);
        java.util.List<Long> accountIds = accounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        log.info("Found {} accounts with role {} excluding {}", accountIds.size(), roleName, excludeRoleName);
        return accountIds;
    }

    @Transactional
    public ChangePasswordResponse changePassword(HttpServletRequest request, ChangePasswordRequest changePasswordRequest) {
        log.info("Processing change password request");

        // Extract token from Authorization header
        String token = extractTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            throw new AuthenticationException("No authorization token provided");
        }

        // Extract username from token
        String username = jwtService.extractUsername(token);
        log.info("Change password request for user: {}", username);

        // Validate new password and confirm password match
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            log.warn("New password and confirm password do not match for user: {}", username);
            throw new AuthenticationException("New password and confirm password do not match");
        }

        // Get account from database
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User account not found"));

        // Verify current password
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), account.getPassword())) {
            log.warn("Current password is incorrect for user: {}", username);
            throw new AuthenticationException("Current password is incorrect");
        }

        // Check if new password is the same as current password
        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), account.getPassword())) {
            log.warn("New password is the same as current password for user: {}", username);
            throw new AuthenticationException("New password must be different from current password");
        }

        // Update password
        account.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        accountRepository.save(account);

        log.info("Password changed successfully for user: {}", username);

        return new ChangePasswordResponse(
                "Password changed successfully",
                username,
                Instant.now().toString()
        );
    }

    @Transactional(readOnly = true)
    public SendLoginConfirmationResponse sendLoginConfirmation(SendLoginConfirmationRequest request, HttpServletRequest httpRequest) {
        log.info("Processing send login confirmation request for username: {}", request.getUsername());

        // Get account from database
        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("User account not found"));

        Set<String> roles = account.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        String userType = roles.isEmpty() ? "USER" : roles.iterator().next();

        // Publish login confirmation event
        authEventService.publishLoginConfirmationEvent(
            account.getId(),
            account.getUsername(),
            request.getEmail(),
            userType,
            httpRequest
        );

        log.info("Login confirmation event published for user: {}", request.getUsername());

        return new SendLoginConfirmationResponse(
            "Login confirmation email sent successfully",
            request.getEmail(),
            Instant.now().toString()
        );
    }

    public ConfirmLoginResponse confirmLogin(String token) {
        log.info("Processing login confirmation with token");

        try {
            // Validate JWT confirmation token
            Claims claims = jwtService.validateConfirmationToken(token);

            Long accountId = claims.get("accountId", Long.class);
            String email = claims.get("email", String.class);

            // Get account info
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new AuthenticationException("Account not found"));

            // Publish WebSocket notification event
            com.devwonder.common.event.LoginConfirmationNotificationEvent notificationEvent =
                    com.devwonder.common.event.LoginConfirmationNotificationEvent.builder()
                            .accountId(accountId)
                            .username(account.getUsername())
                            .message("Your login has been confirmed successfully")
                            .confirmedAt(LocalDateTime.now())
                            .build();

            authEventService.publishLoginConfirmedNotification(notificationEvent);

            log.info("Login confirmed successfully for accountId: {}", accountId);

            return new ConfirmLoginResponse(
                    "Login confirmed successfully",
                    accountId,
                    account.getUsername(),
                    Instant.now().toString()
            );

        } catch (Exception e) {
            log.error("Login confirmation failed: {}", e.getMessage());
            throw new AuthenticationException("Invalid or expired confirmation token");
        }
    }
}