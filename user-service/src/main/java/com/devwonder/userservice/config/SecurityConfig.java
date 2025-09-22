package com.devwonder.userservice.config;

import com.devwonder.common.config.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@Configuration
public class SecurityConfig extends BaseSecurityConfig {

    @Override
    protected void configureServiceEndpoints(AuthorizeHttpRequestsConfigurer<org.springframework.security.config.annotation.web.builders.HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
            // Customer endpoints - Inter-service access with API key
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
