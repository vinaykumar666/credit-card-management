package com.cards.auth.security;

import com.cards.auth.config.CorrelationIdFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Default Spring Security filter chain for the authentication service (runs after the AS chain).
 * Opens health, docs, OAuth2 discovery, and public auth endpoints; other routes require a JWT.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorrelationIdFilter correlationIdFilter;

    /**
     * Configures CSRF off, CORS, form login, JWT resource-server validation, and public paths.
     * Also registers the correlation-id filter before username/password authentication.
     *
     * @param http the {@link HttpSecurity} builder
     * @return the default {@link SecurityFilterChain} at order 2
     * @throws Exception if Spring Security configuration fails
     */
    @Bean
    @Order(2)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/.well-known/**", "/oauth2/**", "/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login",
                                "/api/v1/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/validate").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .formLogin(Customizer.withDefaults())
                .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Provides a delegating password encoder for user passwords and OAuth2 client secrets.
     * Supports prefixes such as {@code {bcrypt}} and {@code {noop}} used in YAML client secrets.
     *
     * @return a {@link PasswordEncoder} that delegates by id prefix
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        // Supports {bcrypt} and {noop} prefixes used by OAuth2 client secrets in YAML
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
