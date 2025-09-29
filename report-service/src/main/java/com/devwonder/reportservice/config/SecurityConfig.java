package com.devwonder.reportservice.config;

import com.devwonder.common.config.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@Configuration
public class SecurityConfig extends BaseSecurityConfig {

    @Override
    protected void configureServiceEndpoints(AuthorizeHttpRequestsConfigurer<org.springframework.security.config.annotation.web.builders.HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
            // Dashboard endpoints - accessible via API key or Gateway (must come before /report/**)
            .requestMatchers("/report/dashboard/**").access(authApiKeyOrGatewayRequired())
            // All other report endpoints - ONLY accessible via API Gateway
            .requestMatchers("/report/**").access(gatewayHeaderRequired());
    }
}
