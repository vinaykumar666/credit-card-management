package com.cards.common.error;

import lombok.Builder;

import java.time.Instant;

/**
 * Standard API error body returned by services when a request fails.
 * Built via Lombok {@code @Builder} so callers can set optional correlation and tenant fields.
 *
 * @param timestamp     when the error was created
 * @param status        HTTP status code
 * @param errorCode     catalog error code (for example {@code AUTH_001})
 * @param message       human-readable error message
 * @param path          request path that failed
 * @param correlationId request correlation id, if available
 * @param channelId     channel (tenant) id, if available
 * @param clientId      client id, if available
 */
@Builder
public record ErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        String correlationId,
        String channelId,
        String clientId
) {
}
