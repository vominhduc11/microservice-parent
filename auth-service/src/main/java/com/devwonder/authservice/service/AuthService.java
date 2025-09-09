package com.devwonder.authservice.service;

import com.devwonder.authservice.dto.LoginRequest;
import com.devwonder.authservice.dto.LoginResponse;
import com.devwonder.authservice.entity.Account;
import com.devwonder.authservice.entity.Role;
import com.devwonder.authservice.repository.AccountRepository;
import com.devwonder.common.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        // Find account by username
        Account account = accountRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));


        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), account.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
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

        // Generate JWT token
        String token = jwtService.generateToken(account.getUsername(), claims);

        // Token expires in 24 hours (86400 seconds)
        Long expiresIn = 86400L;

        return new LoginResponse(token, expiresIn, account.getUsername(), roles);
    }
}