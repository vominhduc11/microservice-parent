package com.devwonder.api_gateway.config;

import java.util.*;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import com.devwonder.api_gateway.security.AllAuthoritiesAuthorizationManager;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .oauth2ResourceServer(this::configureOAuth2ResourceServer)
                .authorizeExchange(this::configureAuthorization)
                .build();
    }

    private void configureOAuth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec oauth2) {
        oauth2.jwt(jwt -> jwt
                .jwkSetUri("http://auth-service:8081/auth/.well-known/jwks.json")
                .jwtAuthenticationConverter(jwtAuthenticationConverter()));
    }

    private void configureAuthorization(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
                // CORS preflight requests - HIGHEST PRIORITY
                .pathMatchers(HttpMethod.OPTIONS).permitAll();

        // Configure public endpoints
        configurePublicEndpoints(exchanges);

        // Configure service-specific authorization
        configureProductServiceAuth(exchanges);
        configureBlogServiceAuth(exchanges);
        configureUserServiceAuth(exchanges);
        configureCartServiceAuth(exchanges);
        configureOrderServiceAuth(exchanges);
        configureWarrantyServiceAuth(exchanges);
        configureNotificationServiceAuth(exchanges);
        configureReportServiceAuth(exchanges);

        // Deny all other requests
        exchanges.anyExchange().denyAll();
    }

    private void configurePublicEndpoints(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
                // Swagger UI - public access for development
                .pathMatchers(getSwaggerPaths()).permitAll()
                // Auth Service - public access
                .pathMatchers("/api/auth/**").permitAll();
    }

    private void configureProductServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        // TODO: Add product service authorization rules when endpoints are implemented
    }

    private void configureBlogServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        // TODO: Add blog service authorization rules when endpoints are implemented
    }

    private void configureUserServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        // TODO: Add user service authorization rules when endpoints are implemented
    }

    private void configureCartServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        // TODO: Add cart service authorization rules when endpoints are implemented
    }

    private void configureOrderServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        // TODO: Add order service authorization rules when endpoints are implemented
    }

    private void configureWarrantyServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        // TODO: Add warranty service authorization rules when endpoints are implemented
    }

    private void configureNotificationServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        // TODO: Add notification service authorization rules when endpoints are implemented
    }

    private void configureReportServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        // TODO: Add report service authorization rules when endpoints are implemented
    }

    private String[] getSwaggerPaths() {
        return new String[] {
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/webjars/**",
                "/webjars/swagger-ui/**",
                "/v3/api-docs/**",
                "/api/*/v3/api-docs/**",
                "/api/*/swagger-ui/**",
                "/api/*/webjars/**",
                "/api/user/v3/api-docs/**",
                "/api/user/swagger-ui/**",
                "/api/user/webjars/**",
                "/auth/swagger-ui.html",
                "/auth/swagger-ui/**",
                "/auth/webjars/**",
                "/auth/v3/api-docs"
        };
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return new ReactiveJwtAuthenticationConverterAdapter(jwtConverter);
    }

    private Collection<org.springframework.security.core.GrantedAuthority> extractAuthorities(
            org.springframework.security.oauth2.jwt.Jwt jwt) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        // Extract roles
        extractRoles(jwt, authorities);

        // Extract permissions
        extractPermissions(jwt, authorities);

        return authorities.isEmpty()
                ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                : new ArrayList<>(authorities);
    }

    private void extractRoles(org.springframework.security.oauth2.jwt.Jwt jwt,
            Set<SimpleGrantedAuthority> authorities) {
        Object rolesObj = jwt.getClaim("roles");
        if (rolesObj instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<String> rolesList = (java.util.List<String>) rolesObj;
            rolesList.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .forEach(authorities::add);
        }
    }

    private void extractPermissions(org.springframework.security.oauth2.jwt.Jwt jwt,
            Set<SimpleGrantedAuthority> authorities) {
        Object permsObj = jwt.getClaim("permissions");
        if (permsObj instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<String> permsList = (java.util.List<String>) permsObj;
            permsList.stream()
                    .map(perm -> new SimpleGrantedAuthority("PERM_" + perm))
                    .forEach(authorities::add);
        }
    }
}