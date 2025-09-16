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

    private static final String ROLE_ADMIN = "ADMIN";

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> {}) // Use default CORS config from YAML
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
        configureMediaServiceAuth(exchanges);
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
                
                // Token validation endpoint - requires valid JWT token
                .pathMatchers(HttpMethod.GET, "/api/auth/validate").authenticated()

                // JWKS endpoint - public access (for other services to validate tokens)
                .pathMatchers(HttpMethod.GET, "/api/auth/.well-known/jwks.json").permitAll();
    }

    private void configureProductServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
            // ADMIN-only product endpoints (authentication + ADMIN role required) - MUST BE FIRST
            .pathMatchers(HttpMethod.GET, "/api/product/products").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.POST, "/api/product/products").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.PATCH, "/api/product/{id}").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/product/{id}").hasRole(ROLE_ADMIN)

            // Product Serial endpoints - ADMIN only
            .pathMatchers(HttpMethod.POST, "/api/product/serial").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.POST, "/api/product/serials").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/product/serial/*").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.PATCH, "/api/product/serial/*/status").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.GET, "/api/product/{productId}/serials").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.GET, "/api/product/{productId}/inventory").hasRole(ROLE_ADMIN)

            // Public product endpoints (no authentication required) - AFTER specific rules
            .pathMatchers(HttpMethod.GET, "/api/product/products/showhomepageandlimit4").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/product/products/featuredandlimit1").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/product/{id}").permitAll();
    }

    private void configureBlogServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
            // ADMIN-only blog endpoints (authentication + ADMIN role required) - MUST BE FIRST
            .pathMatchers(HttpMethod.GET, "/api/blog/blogs").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.POST, "/api/blog/blogs").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.PATCH, "/api/blog/{id}").hasRole(ROLE_ADMIN)

            // Public blog endpoints (no authentication required) - AFTER specific rules
            .pathMatchers(HttpMethod.GET, "/api/blog/blogs/showhomepageandlimit6").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/blog/{id}").permitAll();
    }

    private void configureUserServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
                // Dealers endpoint - public access (for displaying dealer network)
                .pathMatchers(HttpMethod.GET, "/api/user/dealers").permitAll()
                
                // Dealer registration - public access (for dealer self-registration)
                .pathMatchers(HttpMethod.POST, "/api/user/dealers").permitAll()
                
                // Update dealer - ADMIN only
                .pathMatchers(HttpMethod.PUT, "/api/user/dealers/*").hasRole(ROLE_ADMIN)

                // Delete dealer - ADMIN only
                .pathMatchers(HttpMethod.DELETE, "/api/user/dealers/*").hasRole(ROLE_ADMIN);
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
            .pathMatchers(HttpMethod.GET, "/api/notification/notifies").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.PATCH, "/api/notification/*/read").hasRole(ROLE_ADMIN);
    }

    private void configureMediaServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
            // ADMIN-only media endpoints (authentication + ADMIN role required)
            .pathMatchers(HttpMethod.POST, "/api/media/upload/image").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.POST, "/api/media/upload/video").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/media/delete").hasRole(ROLE_ADMIN);
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

    // CORS configuration moved to YAML (api-gateway.yml) for easier maintenance
}