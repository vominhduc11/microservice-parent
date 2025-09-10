package com.devwonder.apigateway.config;

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
                .pathMatchers(HttpMethod.OPTIONS).permitAll()

                // Swagger UI and API docs - PUBLIC ACCESS (No Authentication Required)
                .pathMatchers(getSwaggerPaths()).permitAll()

                // Actuator endpoints - public access
                .pathMatchers("/actuator/**").permitAll();

        // Configure service-specific authorization BEFORE general rules
        configureAuthServiceAuth(exchanges);
        configureProductServiceAuth(exchanges);
        configureBlogServiceAuth(exchanges);
        configureUserServiceAuth(exchanges);
        configureCartServiceAuth(exchanges);
        configureOrderServiceAuth(exchanges);
        configureWarrantyServiceAuth(exchanges);
        configureNotificationServiceAuth(exchanges);
        configureReportServiceAuth(exchanges);

        // All other requests require authentication - MUST BE LAST
        exchanges.anyExchange().denyAll();
    }

    private void configureAuthServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
                // Login endpoint - public access (no authentication required)
                .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                // Logout endpoint - requires valid JWT token
                .pathMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()

                // Refresh token endpoint - public access (handles expired tokens internally)
                .pathMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()

                // JWKS endpoint - public access (for other services to validate tokens)
                .pathMatchers(HttpMethod.GET, "/api/auth/.well-known/jwks.json").permitAll();
    }

    private void configureProductServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
            .pathMatchers(HttpMethod.GET, "/api/product/products/showhomepageandlimit4").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/product/{id}").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/product/products/featuredandlimit1").permitAll();
    }

    private void configureBlogServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        // TODO: Add blog service authorization rules when endpoints are implemented
    }

    private void configureUserServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
                // Dealers endpoint - public access (for displaying dealer network)
                .pathMatchers(HttpMethod.GET, "/api/user/dealers").permitAll()
                
                // Dealer registration - public access (for dealer self-registration)
                .pathMatchers(HttpMethod.POST, "/api/user/dealers").permitAll();
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
        exchanges
            .pathMatchers(HttpMethod.GET, "/api/notification/notifies").hasRole("ADMIN");
    }

    private void configureReportServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        // TODO: Add report service authorization rules when endpoints are implemented
    }

    private String[] getSwaggerPaths() {
        return new String[] {
                // Essential Swagger UI paths only - Optimized for centralized access
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/webjars/**",
                // API docs for all services
                "/api/*/v3/api-docs/**"
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

        // Extract roles only
        extractRoles(jwt, authorities);

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
}