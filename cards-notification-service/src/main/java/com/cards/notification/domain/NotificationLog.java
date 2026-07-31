package com.cards.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for a notification delivery attempt stored in {@code notification_log}.
 * Tracks channel, template, recipient, status, and any send error.
 */
@Entity
@Table(name = "notification_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    /** Unique notification log identifier. */
    @Id
    private UUID id;

    /** Optional user this notification is for. */
    @Column(name = "user_id")
    private UUID userId;

    /** Delivery channel such as EMAIL, SMS, or PUSH. */
    @Column(nullable = false, length = 20)
    private String channel;

    /** Template key used to render the message. */
    @Column(nullable = false, length = 100)
    private String template;

    /** Destination address or device target. */
    @Column(nullable = false)
    private String recipient;

    /** Serialized payload or placeholder data. */
    @Column(columnDefinition = "TEXT")
    private String payload;

    /** Delivery status such as PENDING, SENT, or FAILED. */
    @Column(nullable = false, length = 20)
    private String status;

    /** Correlation id used for request tracing. */
    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    /** Truncated error message when send fails. */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /** When the log row was created. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** When the notification was successfully sent, if ever. */
    @Column(name = "sent_at")
    private Instant sentAt;

    /**
     * Assigns an id and created timestamp before the first persist when missing.
     */
    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
