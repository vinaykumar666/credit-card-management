package com.cards.notification.service;

import com.cards.common.correlation.CorrelationConstants;
import com.cards.common.event.NotificationRequestedEvent;
import com.cards.notification.domain.NotificationLog;
import com.cards.notification.repository.NotificationLogRepository;
import com.cards.notification.sender.NotificationFactory;
import com.cards.notification.sender.NotificationSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates notification delivery and persistence.
 * Saves a PENDING log, sends via the channel factory, then updates the log to SENT or FAILED.
 */
@Service
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final NotificationLogRepository notificationLogRepository;
    private final NotificationFactory notificationFactory;
    private final ObjectMapper objectMapper;

    /**
     * Creates the dispatch service with its persistence, factory, and JSON dependencies.
     *
     * @param notificationLogRepository repository for notification logs
     * @param notificationFactory       factory that resolves channel senders
     * @param objectMapper              mapper used to serialize placeholders
     */
    public NotificationDispatchService(
            NotificationLogRepository notificationLogRepository,
            NotificationFactory notificationFactory,
            ObjectMapper objectMapper
    ) {
        this.notificationLogRepository = notificationLogRepository;
        this.notificationFactory = notificationFactory;
        this.objectMapper = objectMapper;
    }

    /**
     * Dispatches a notification from a requested event and returns the final log row.
     *
     * @param event notification request including channel, template, and recipient
     * @return saved log with SENT or FAILED status
     */
    @Transactional
    public NotificationLog dispatch(NotificationRequestedEvent event) {
        String payload = toPayload(event.getPlaceholders());
        NotificationLog notificationLog = NotificationLog.builder()
                .id(event.getNotificationId() != null ? event.getNotificationId() : UUID.randomUUID())
                .userId(event.getUserId())
                .channel(event.getChannel())
                .template(event.getTemplate())
                .recipient(event.getRecipient())
                .payload(payload)
                .status("PENDING")
                .correlationId(event.getCorrelationId())
                .createdAt(Instant.now())
                .build();

        notificationLog = notificationLogRepository.save(notificationLog);

        try {
            NotificationSender sender = notificationFactory.getSender(event.getChannel());
            sender.send(event.getRecipient(), event.getTemplate(), payload);
            notificationLog.setStatus("SENT");
            notificationLog.setSentAt(Instant.now());
            notificationLog.setErrorMessage(null);
        } catch (Exception ex) {
            log.error("Failed to send notification id={} channel={}", notificationLog.getId(), event.getChannel(), ex);
            notificationLog.setStatus("FAILED");
            notificationLog.setErrorMessage(truncate(ex.getMessage(), 500));
        }

        return notificationLogRepository.save(notificationLog);
    }

    /**
     * Builds and dispatches an EMAIL notification for a payment-related event.
     * Uses a synthetic {@code userId@cards.local} recipient when a user id is present.
     *
     * @param userId        user receiving the payment email (may be null)
     * @param template      email template key
     * @param placeholders  values substituted into the template
     * @param correlationId tracing id, or falls back to MDC when blank
     * @return saved notification log after dispatch
     */
    @Transactional
    public NotificationLog dispatchEmailForPayment(
            UUID userId,
            String template,
            Map<String, String> placeholders,
            String correlationId
    ) {
        String resolvedCorrelationId = correlationId != null && !correlationId.isBlank()
                ? correlationId
                : MDC.get(CorrelationConstants.MDC_CORRELATION_ID);

        NotificationRequestedEvent event = NotificationRequestedEvent.builder()
                .notificationId(UUID.randomUUID())
                .userId(userId)
                .channel("EMAIL")
                .template(template)
                .recipient(userId != null ? userId + "@cards.local" : "unknown@cards.local")
                .placeholders(placeholders)
                .correlationId(resolvedCorrelationId)
                .requestedAt(Instant.now())
                .build();

        return dispatch(event);
    }

    /**
     * Serializes placeholders to JSON, or falls back to {@code toString()} on failure.
     *
     * @param placeholders template placeholder map
     * @return JSON string, map string, or null when empty
     */
    private String toPayload(Map<String, String> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(placeholders);
        } catch (JsonProcessingException ex) {
            return placeholders.toString();
        }
    }

    /**
     * Truncates a string to at most {@code max} characters.
     *
     * @param value original string (may be null)
     * @param max   maximum length
     * @return truncated string, original if short enough, or null
     */
    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
