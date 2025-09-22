package com.devwonder.productservice.config;

import com.devwonder.common.config.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends BaseSecurityConfig {

    @Override
    protected void configureServiceEndpoints(AuthorizeHttpRequestsConfigurer<org.springframework.security.config.annotation.web.builders.HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
            // Specific product endpoints for inter-service calls - API key required
            .requestMatchers("/product-serial/serial/*").access(authApiKeyRequired())       // Serial lookup calls
            .requestMatchers("/product-serial/bulk-status").access(authApiKeyRequired())    // Bulk status update calls

            // All other product endpoints - ONLY accessible via API Gateway
            .requestMatchers("/product/**").access(gatewayHeaderRequired());
    }
}
