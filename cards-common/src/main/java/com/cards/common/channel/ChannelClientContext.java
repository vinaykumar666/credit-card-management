package com.cards.common.channel;

/**
 * Immutable request-scoped tenant identity taken from headers (channel, client, correlation).
 * Null or blank values are normalized to empty strings in the compact constructor.
 *
 * @param channelId     channel / partner id ({@code X-Channel-Id})
 * @param clientId      client application id ({@code X-Client-Id})
 * @param correlationId request correlation id ({@code X-Correlation-Id})
 */
public record ChannelClientContext(String channelId, String clientId, String correlationId) {

    public ChannelClientContext {
        channelId = channelId == null ? "" : channelId.trim();
        clientId = clientId == null ? "" : clientId.trim();
        correlationId = correlationId == null ? "" : correlationId.trim();
    }

    /**
     * Whether a non-blank channel id is present.
     *
     * @return true if {@code channelId} is not blank
     */
    public boolean hasChannel() {
        return !channelId.isBlank();
    }

    /**
     * Whether a non-blank client id is present.
     *
     * @return true if {@code clientId} is not blank
     */
    public boolean hasClient() {
        return !clientId.isBlank();
    }
}
