package com.devwonder.userservice.config;

import com.devwonder.common.config.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@Configuration
public class SecurityConfig extends BaseSecurityConfig {

    @Override
    protected void configureServiceEndpoints(AuthorizeHttpRequestsConfigurer<org.springframework.security.config.annotation.web.builders.HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
            // Inter-service lookup endpoints - API key required
            .requestMatchers("/customer-service/**").access(authApiKeyRequired())
            .requestMatchers("/dealer-service/**").access(authApiKeyRequired())

            // Gateway endpoints - ONLY accessible via API Gateway
            .requestMatchers("/customer/**").access(gatewayHeaderRequired())
            .requestMatchers("/dealer/**").access(gatewayHeaderRequired())
            .requestMatchers("/admin/**").access(gatewayHeaderRequired());
    }
}
