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
            .requestMatchers("/user-service/**").access(authApiKeyRequired())

            // All user and customer endpoints - ONLY accessible via API Gateway
            .requestMatchers("/user/**").access(gatewayHeaderRequired())
            .requestMatchers("/customer/**").access(gatewayHeaderRequired());
    }
}
