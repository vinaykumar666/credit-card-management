package com.cards.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configures Cross-Origin Resource Sharing (CORS) for the authentication service.
 * Allows local frontend and BFF origins to call auth APIs with credentials and custom headers.
 */
@Configuration
public class CorsConfig {

    /**
     * Builds the CORS rules applied to all HTTP paths.
     * Permits localhost Angular and BFF origins, common HTTP methods, and correlation/channel headers.
     *
     * @return a {@link CorsConfigurationSource} registered for {@code /**}
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://localhost:8088",
                "http://127.0.0.1:4200",
                "http://127.0.0.1:8088"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("X-Correlation-Id", "X-Channel-Id", "X-Client-Id"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
