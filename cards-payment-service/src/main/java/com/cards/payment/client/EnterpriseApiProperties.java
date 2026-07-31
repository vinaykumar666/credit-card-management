package com.cards.payment.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the enterprise payment API client.
 * Bound from {@code app.enterprise-api} properties.
 *
 * @param baseUrl base URL of the enterprise payment API
 */
@ConfigurationProperties(prefix = "app.enterprise-api")
public record EnterpriseApiProperties(String baseUrl) {
}
