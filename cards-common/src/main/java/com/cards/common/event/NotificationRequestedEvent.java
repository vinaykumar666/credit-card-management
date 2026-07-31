package com.cards.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka event payload asking the notification service to send a message.
 * Includes template name, recipient, channel, and placeholder values for rendering.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestedEvent {

    /** Unique id for this notification request. */
    private UUID notificationId;
    /** User who should receive the notification. */
    private UUID userId;
    /** Delivery channel (for example email or SMS). */
    private String channel;
    /** Template key used to render the message. */
    private String template;
    /** Destination address (email, phone, etc.). */
    private String recipient;
    /** Key/value pairs substituted into the template. */
    private Map<String, String> placeholders;
    /** Correlation id for tracing across services. */
    private String correlationId;
    /** When the notification was requested. */
    private Instant requestedAt;
}
