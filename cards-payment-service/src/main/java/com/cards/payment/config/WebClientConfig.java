package com.cards.payment.config;

import com.cards.payment.client.EnterpriseApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configures the {@link WebClient} used to call the enterprise payment API.
 * Also enables binding of {@link EnterpriseApiProperties}.
 */
@Configuration
@EnableConfigurationProperties(EnterpriseApiProperties.class)
public class WebClientConfig {

    /**
     * Creates a WebClient pointed at the enterprise API base URL.
     *
     * @param properties enterprise API settings (base URL)
     * @param builder    Spring-provided WebClient builder
     * @return WebClient for enterprise payment calls
     */
    @Bean
    public WebClient enterpriseWebClient(EnterpriseApiProperties properties, WebClient.Builder builder) {
        return builder
                .baseUrl(properties.baseUrl())
                .build();
    }
}
