```java
package com.bank.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the Banking Core Service.
 *
 * <p>Defines the HTTP security filter chain and enables method-level security
 * so that {@code @PreAuthorize} annotations on controllers and services are honoured.
 *
 * <p>Security rules (in evaluation order):
 * <ol>
 *   <li>Swagger / OpenAPI UI paths — publicly accessible (documentation only).</li>
 *   <li>Actuator health endpoint — publicly accessible (liveness/readiness probes).</li>
 *   <li>Authentication endpoints ({@code /api/v1/auth/**}) — publicly accessible.</li>
 *   <li>Dashboard endpoints ({@code /api/v1/dashboard/**}) — require a valid JWT (authenticated).</li>
 *   <li>All other requests — require authentication.</li>
 * </ol>
 *
 * <p>Session management is stateless; CSRF protection is disabled because the API
 * is consumed by clients that authenticate via Bearer JWT on every request.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * Paths that are always publicly accessible (no authentication required).
     */
    private static final String[] PUBLIC_PATHS = {
            // Springdoc / Swagger UI
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            // Spring Boot Actuator — health probe only
            "/actuator/health",
            // Authentication (login / token refresh)
            "/api/v1/auth/**"
    };

    /**
     * Configures the primary {@link SecurityFilterChain}.
     *
     * <p>Key decisions:
     * <ul>
     *   <li>CSRF is disabled — the service is a stateless REST API protected by JWT.</li>
     *   <li>Session creation policy is {@code STATELESS} — no HTTP session is created or used.</li>
     *   <li>{@code /api/v1/dashboard/**} requires an authenticated principal; anonymous access
     *       is explicitly denied by the {@code authenticated()} rule combined with the
     *       {@code SessionCreationPolicy.STATELESS} policy (no anonymous session is created).</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} builder provided by Spring Security
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the configuration cannot be applied
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — stateless JWT API; CSRF tokens are not applicable
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless session management — no HttpSession is created or consulted
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorisation rules — evaluated in declaration order (most specific first)
                .authorizeHttpRequests(auth -> auth

                        // Public paths: Swagger UI, Actuator health, Auth endpoints
                        .requestMatchers(PUBLIC_PATHS).permitAll()

                        // Dashboard endpoints: valid JWT required — no anonymous access permitted
                        .requestMatchers("/api/v1/dashboard/**").authenticated()

                        // All remaining endpoints: authentication required
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
```