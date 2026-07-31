package com.cards.bff.web.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Notification record returned to UI clients by the BFF.
 *
 * @param id            notification identifier
 * @param userId        target user
 * @param channel       delivery channel (for example email or SMS)
 * @param template      template name used to render the message
 * @param recipient     destination address or endpoint
 * @param payload       message payload content
 * @param status        delivery status
 * @param correlationId correlation ID for tracing
 * @param errorMessage  failure detail when status indicates an error
 * @param createdAt     creation timestamp
 * @param sentAt        send timestamp when delivered
 */
public record NotificationDto(
        UUID id,
        UUID userId,
        String channel,
        String template,
        String recipient,
        String payload,
        String status,
        String correlationId,
        String errorMessage,
        Instant createdAt,
        Instant sentAt
) {
}
