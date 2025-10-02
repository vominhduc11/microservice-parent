package com.devwonder.apigateway.config;

import java.util.*;

import com.devwonder.apigateway.security.AllAuthoritiesAuthorizationManager;
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
    private static final String ROLE_DEALER = "DEALER";
    private static final String ROLE_SYSTEM = "SYSTEM";

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

                // Change password endpoint - requires ADMIN role
                .pathMatchers(HttpMethod.POST, "/api/auth/change-password").hasRole(ROLE_ADMIN)

                // Send login confirmation email - requires ADMIN role
                .pathMatchers(HttpMethod.POST, "/api/auth/send-login-confirmation").hasRole(ROLE_ADMIN)

                // Confirm login from email - public access (validates JWT internally)
                .pathMatchers(HttpMethod.GET, "/api/auth/confirm-login").permitAll()

                // JWKS endpoint - public access (for other services to validate tokens)
                .pathMatchers(HttpMethod.GET, "/api/auth/.well-known/jwks.json").permitAll();
    }

    private void configureProductServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
            // ADMIN/DEALER product endpoints (authentication + ADMIN or DEALER role required) - MUST BE FIRST
            .pathMatchers(HttpMethod.GET, "/api/product/products").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/product/products/deleted").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.POST, "/api/product/products").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.PATCH, "/api/product/{id}").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.PATCH, "/api/product/{id}/restore").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/product/{id}").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/product/{id}/hard").hasRole(ROLE_ADMIN)

            // Product Serial endpoints - ADMIN only
            .pathMatchers(HttpMethod.POST, "/api/product/product-serials/serial").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.POST, "/api/product/product-serials/serials").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/product/product-serials/serial/*").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/product/product-serials/serials").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.PATCH, "/api/product/product-serials/serial/*/status").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.GET, "/api/product/product-serials/{productId}/serials").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.GET, "/api/product/product-serials/{productId}/serials/status/*").hasAnyRole(ROLE_ADMIN, ROLE_DEALER)
            .pathMatchers(HttpMethod.GET, "/api/product/product-serials/{productId}/inventory").hasRole(ROLE_ADMIN)

            // Product Serial assignment endpoints - ADMIN only
            .pathMatchers(HttpMethod.POST, "/api/product/product-serials/serials/assign-to-order-item/*").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.PATCH, "/api/product/product-serials/serials/unassign-from-order-item/*").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.POST, "/api/product/product-serials/serials/allocate-to-dealer/*").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.GET, "/api/product/product-serials/order-items/*/serials").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.GET, "/api/product/product-serials/order-items/*/allocated-serials").hasRole(ROLE_ADMIN)

            // Product Serial available count - DEALER only access
            .pathMatchers(HttpMethod.GET, "/api/product/product-serials/{productId}/available-count").hasRole(ROLE_DEALER)

            // Product Serial endpoints by product and dealer - DEALER only access
            .pathMatchers(HttpMethod.GET, "/api/product/product-serials/product/*/dealer/*/serials").hasRole(ROLE_DEALER)

            // Public product endpoints (no authentication required) - AFTER specific rules
            .pathMatchers(HttpMethod.GET, "/api/product/products/homepage").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/product/products/featured").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/product/products/related/{id}").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/product/products/search").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/product/{id}").permitAll();
    }

    private void configureBlogServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
            // ADMIN-only blog endpoints (authentication + ADMIN role required) - MUST BE FIRST
            .pathMatchers(HttpMethod.GET, "/api/blog/blogs").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/blog/blogs/deleted").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.POST, "/api/blog/blogs").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.PATCH, "/api/blog/{id}").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.PATCH, "/api/blog/{id}/restore").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/blog/{id}").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/blog/{id}/hard").hasRole(ROLE_ADMIN)

            // ADMIN-only category blog endpoints
            .pathMatchers(HttpMethod.POST, "/api/blog/categories").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/blog/categories/*").hasRole(ROLE_ADMIN)

            // Public blog endpoints (no authentication required) - AFTER specific rules
            .pathMatchers(HttpMethod.GET, "/api/blog/categories").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/blog/categories/*/blogs").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/blog/blogs/homepage").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/blog/blogs/related/{id}").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/blog/blogs/search").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/blog/{id}").permitAll();
    }

    private void configureUserServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
                // ADMIN-only dealer search endpoint
                .pathMatchers(HttpMethod.GET, "/api/user/dealer/search").hasRole(ROLE_ADMIN)

                // PUBLIC Dealer endpoints
                .pathMatchers(HttpMethod.GET, "/api/user/dealer").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/user/dealer/*").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/user/dealer").permitAll()



                // ADMIN endpoints - specific rules FIRST

                // Get all admins endpoint - requires both SYSTEM and ADMIN roles
                .pathMatchers(HttpMethod.GET, "/api/user/admin")
                    .access(new AllAuthoritiesAuthorizationManager("ROLE_SYSTEM", "ROLE_ADMIN"))

                // Register admin endpoint - requires both SYSTEM and ADMIN roles
                .pathMatchers(HttpMethod.POST, "/api/user/admin")
                    .access(new AllAuthoritiesAuthorizationManager("ROLE_SYSTEM", "ROLE_ADMIN"))

                .pathMatchers(HttpMethod.GET, "/api/user/admin/dealers/*").hasRole(ROLE_ADMIN)
                .pathMatchers(HttpMethod.PUT, "/api/user/admin/dealers/*").hasRole(ROLE_ADMIN)
                .pathMatchers(HttpMethod.DELETE, "/api/user/admin/dealers/*").hasRole(ROLE_ADMIN)

                // Update login email confirmation setting - ADMIN only
                .pathMatchers(HttpMethod.PATCH, "/api/user/admin/*/login-email-confirmation").hasRole(ROLE_ADMIN)

                // ADMIN general endpoints - AFTER specific rules
                .pathMatchers(HttpMethod.GET, "/api/user/admin/*").hasRole(ROLE_ADMIN)
                .pathMatchers(HttpMethod.PUT, "/api/user/admin/*").hasRole(ROLE_ADMIN)

                // Direct ADMIN endpoints
                .pathMatchers(HttpMethod.GET, "/api/admin/dealers/*").hasRole(ROLE_DEALER)
                .pathMatchers(HttpMethod.PUT, "/api/admin/dealers/*").hasRole(ROLE_ADMIN)
                .pathMatchers(HttpMethod.DELETE, "/api/admin/dealers/*").hasRole(ROLE_ADMIN);
    }

    private void configureCartServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
            // Dealer cart endpoints - DEALER role required
            .pathMatchers(HttpMethod.POST, "/api/cart/items").hasRole(ROLE_DEALER)
            .pathMatchers(HttpMethod.GET, "/api/cart/dealer/*").hasRole(ROLE_DEALER)
            .pathMatchers(HttpMethod.DELETE, "/api/cart/dealer/*").hasRole(ROLE_DEALER)
            .pathMatchers(HttpMethod.DELETE, "/api/cart/items/*").hasRole(ROLE_DEALER)
            .pathMatchers(HttpMethod.PATCH, "/api/cart/items/*/quantity").hasRole(ROLE_DEALER);
    }

    private void configureOrderServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
            // ADMIN-only endpoints - MUST BE FIRST (specific patterns first)
            .pathMatchers(HttpMethod.GET, "/api/order/orders/search").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.GET, "/api/order/orders/deleted").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.PATCH, "/api/order/orders/*/payment-status").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/order/orders/bulk").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/order/orders/bulk/hard").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/order/orders/*").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/order/orders/*/hard").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.PATCH, "/api/order/orders/*/restore").hasRole(ROLE_ADMIN)

            // ADMIN and DEALER endpoints
            .pathMatchers(HttpMethod.GET, "/api/order/orders").hasAnyRole(ROLE_ADMIN, ROLE_DEALER)

            // DEALER endpoints - specific patterns FIRST
            .pathMatchers(HttpMethod.GET, "/api/order/orders/dealer/*/purchased-products").hasRole(ROLE_DEALER)
            .pathMatchers(HttpMethod.POST, "/api/order/orders").hasRole(ROLE_DEALER)
            .pathMatchers(HttpMethod.GET, "/api/order/orders/dealer/*").hasAnyRole(ROLE_ADMIN, ROLE_DEALER)

            // Order detail endpoint - allow both ADMIN and DEALER
            .pathMatchers(HttpMethod.GET, "/api/order/orders/*").hasAnyRole(ROLE_ADMIN, ROLE_DEALER);
    }

    private void configureWarrantyServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
            // PUBLIC warranty check endpoint (no authentication required)
            .pathMatchers(HttpMethod.GET, "/api/warranty/check/**").permitAll()

            // DEALER-only warranty endpoints (authentication + DEALER role required)
            .pathMatchers(HttpMethod.POST, "/api/warranty").hasRole(ROLE_DEALER);
    }

    private void configureNotificationServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
            .pathMatchers(HttpMethod.GET, "/api/notification/notifies").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.PATCH, "/api/notification/*/read").hasRole(ROLE_ADMIN);
    }

    private void configureMediaServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
            // ADMIN-only media endpoints (authentication + ADMIN role required)
            .pathMatchers(HttpMethod.POST, "/api/media/upload").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/media/delete").hasRole(ROLE_ADMIN);
    }

    private void configureReportServiceAuth(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        exchanges
            // New Reports API endpoints - ADMIN only (for dashboard interface)
            .pathMatchers(HttpMethod.GET, "/api/reports/overview").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.GET, "/api/reports/revenue").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.GET, "/api/reports/dealers").hasRole(ROLE_ADMIN)
            .pathMatchers(HttpMethod.GET, "/api/reports/products").hasRole(ROLE_ADMIN)

            // Dashboard endpoints - ADMIN only
            .pathMatchers(HttpMethod.GET, "/api/report/dashboard/**").hasRole(ROLE_ADMIN);
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
                ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
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