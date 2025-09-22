package com.devwonder.userservice.config;

import com.devwonder.common.config.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@Configuration
public class SecurityConfig extends BaseSecurityConfig {

    @Override
    protected void configureServiceEndpoints(AuthorizeHttpRequestsConfigurer<org.springframework.security.config.annotation.web.builders.HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
            // Lookup endpoints for inter-service calls - API key required
            .requestMatchers("/user-service/customers/**").access(authApiKeyRequired())       // Customer lookup calls
            .requestMatchers("/user-service/dealers/**").access(authApiKeyRequired())         // Dealer lookup calls

            // Customer endpoints - Inter-service access with API key (legacy, keep for compatibility)
            .requestMatchers("/customer").access(authApiKeyRequired())                    // POST /customer - create customer
            .requestMatchers("/customer/*").access(authApiKeyRequired())                  // GET /customer/{id} - get customer name

            // Specific user endpoints for dual access - API key OR Gateway header
            .requestMatchers("/user/customers/*/check-exists").access(authApiKeyOrGatewayRequired())  // Inter-service + Frontend

            // Specific user endpoints for inter-service calls - API key required
            .requestMatchers("/user/dealers/*").access(authApiKeyRequired())                 // Inter-service dealer info

            // All other user endpoints - ONLY accessible via API Gateway
            .requestMatchers("/user/**").access(gatewayHeaderRequired());
    }
}
