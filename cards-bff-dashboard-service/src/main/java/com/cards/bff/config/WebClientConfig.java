package com.cards.bff.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Registers WebClient and enables {@link AppProperties} for outbound gateway calls.
 * Gives the BFF a single client preconfigured with the gateway base URL.
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class WebClientConfig {

    /**
     * Creates a WebClient pointed at the configured API gateway base URL.
     *
     * @param properties application properties containing {@code gatewayBaseUrl}
     * @param builder    Spring-provided WebClient builder
     * @return WebClient ready for downstream gateway requests
     */
    @Bean
    public WebClient gatewayWebClient(AppProperties properties, WebClient.Builder builder) {
        return builder
                .baseUrl(properties.gatewayBaseUrl())
                .build();
    }
}
