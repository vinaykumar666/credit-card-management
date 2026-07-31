package com.cards.notification.web.dto;

import com.cards.notification.domain.NotificationLog;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable API view of a notification delivery log.
 * Built from {@link NotificationLog} entities for REST responses.
 */
@Value
@Builder
public class NotificationResponse {

    /** Unique notification log identifier. */
    UUID id;
    /** Optional related user. */
    UUID userId;
    /** Delivery channel. */
    String channel;
    /** Template key used. */
    String template;
    /** Destination address or target. */
    String recipient;
    /** Serialized payload. */
    String payload;
    /** Delivery status. */
    String status;
    /** Tracing correlation id. */
    String correlationId;
    /** Error text when send failed. */
    String errorMessage;
    /** When the log was created. */
    Instant createdAt;
    /** When the notification was sent, if successful. */
    Instant sentAt;

    /**
     * Maps a persisted notification log to this API response.
     *
     * @param log notification log entity
     * @return response DTO with the same field values
     */
    public static NotificationResponse from(NotificationLog log) {
        return NotificationResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .channel(log.getChannel())
                .template(log.getTemplate())
                .recipient(log.getRecipient())
                .payload(log.getPayload())
                .status(log.getStatus())
                .correlationId(log.getCorrelationId())
                .errorMessage(log.getErrorMessage())
                .createdAt(log.getCreatedAt())
                .sentAt(log.getSentAt())
                .build();
    }
}
