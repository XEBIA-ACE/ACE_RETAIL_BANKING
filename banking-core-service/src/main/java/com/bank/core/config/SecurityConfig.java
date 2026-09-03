```java
package com.bank.core.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

/**
 * Spring Security configuration for the Banking Core Service.
 *
 * <p>Enforces stateless JWT-based authentication. The filter chain:
 * <ul>
 *   <li>Permits unauthenticated access to public paths (OpenAPI, actuator health, auth endpoints)</li>
 *   <li>Requires authentication for all {@code /api/v1/accounts/**} routes</li>
 *   <li>Returns HTTP 401 (not a redirect) for unauthenticated requests — suitable for SPA / API clients</li>
 *   <li>Creates no HTTP session (STATELESS policy)</li>
 * </ul>
 *
 * <p>Method-level security ({@code @PreAuthorize}) is enabled via {@link EnableMethodSecurity}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * Paths that are publicly accessible without a valid JWT.
     * Extend this list as new public endpoints are introduced.
     */
    private static final String[] PUBLIC_PATHS = {
            // OpenAPI / Swagger UI
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            // Spring Boot Actuator — health probe only
            "/actuator/health",
            "/actuator/info",
            // Authentication endpoints (login / token refresh)
            "/api/v1/auth/**"
    };

    /**
     * Primary security filter chain.
     *
     * @param http the {@link HttpSecurity} builder provided by Spring Security
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for stateless JWT APIs
                .csrf(AbstractHttpConfigurer::disable)

                // No HTTP session should be created or used
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Return HTTP 401 JSON for unauthenticated requests instead of redirecting
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(unauthorizedEntryPoint()))

                // Authorisation rules
                .authorizeHttpRequests(auth -> auth
                        // Allow unauthenticated access to public paths
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        // All account endpoints require a valid, authenticated principal
                        .requestMatchers("/api/v1/accounts/**").authenticated()
                        // Any other request also requires authentication by default
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * Returns an {@link AuthenticationEntryPoint} that writes a plain HTTP 401 response
     * with a JSON error body. This prevents Spring Security from issuing a 302 redirect
     * to a login page, which would break API / SPA consumers.
     *
     * @return the entry point bean
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return new Json401AuthenticationEntryPoint();
    }

    /**
     * {@link AuthenticationEntryPoint} implementation that responds with HTTP 401 and a
     * minimal JSON error payload. No redirect is issued.
     */
    static class Json401AuthenticationEntryPoint implements AuthenticationEntryPoint {

        private static final String ERROR_BODY =
                "{\"status\":401,\"error\":\"Unauthorized\","
                + "\"message\":\"Authentication is required to access this resource.\"}";

        @Override
        public void commence(
                HttpServletRequest request,
                HttpServletResponse response,
                AuthenticationException authException) throws IOException {

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(ERROR_BODY);
        }
    }
}
```