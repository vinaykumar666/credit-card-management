package com.cards.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Typed configuration bound from {@code app.*} properties.
 * Holds the API gateway base URL and the allow-lists for channels and clients.
 *
 * @param gatewayBaseUrl base URL used by the BFF when calling the API gateway
 * @param channels       allowed channel identifiers for tenant validation
 * @param clients        allowed client identifiers for tenant validation
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String gatewayBaseUrl,
        Channels channels,
        Clients clients
) {
    /**
     * Allow-list of channel IDs that may call the BFF.
     *
     * @param allowed channel identifiers (compared case-insensitively after uppercasing)
     */
    public record Channels(List<String> allowed) {
    }

    /**
     * Allow-list of client IDs that may call the BFF.
     *
     * @param allowed client identifiers
     */
    public record Clients(List<String> allowed) {
    }
}
