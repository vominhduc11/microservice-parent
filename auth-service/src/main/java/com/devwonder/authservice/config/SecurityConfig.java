package com.devwonder.authservice.config;

import com.devwonder.common.config.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

@Configuration
public class SecurityConfig extends BaseSecurityConfig {

    @Override
    protected void configureServiceEndpoints(AuthorizeHttpRequestsConfigurer<org.springframework.security.config.annotation.web.builders.HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
            // JWKS endpoint - PUBLIC access (no authentication required)
            .requestMatchers("/auth/.well-known/jwks.json").permitAll()
            // Account creation endpoint - INTERNAL service-to-service calls only
            .requestMatchers("/auth/accounts").access(internalServiceRequired())
            // Account deletion endpoint - INTERNAL service-to-service calls only
            .requestMatchers("/auth/accounts/*").access(internalServiceRequired())
            // Login endpoint - PUBLIC access via API Gateway
            .requestMatchers("/auth/login").access(gatewayHeaderRequired())
            // Logout endpoint - PUBLIC access via API Gateway
            .requestMatchers("/auth/logout").access(gatewayHeaderRequired())
            // Refresh endpoint - PUBLIC access via API Gateway
            .requestMatchers("/auth/refresh").access(gatewayHeaderRequired())
            // All other auth endpoints - ONLY accessible via API Gateway
            .requestMatchers("/auth/**").access(gatewayHeaderRequired());
    }
    
    /**
     * Requires X-Internal-Service header for internal service-to-service calls
     */
    private WebExpressionAuthorizationManager internalServiceRequired() {
        return new WebExpressionAuthorizationManager("request.getHeader('X-Internal-Service') == 'user-service'");
    }
}