package com.cards.notification.kafka;

import com.cards.common.correlation.CorrelationConstants;
import com.cards.common.event.NotificationRequestedEvent;
import com.cards.common.event.PaymentCompletedEvent;
import com.cards.common.event.PaymentFailedEvent;
import com.cards.common.kafka.KafkaTopics;
import com.cards.notification.service.NotificationDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kafka listeners that turn payment and notification events into outbound notifications.
 * Sets the correlation id in MDC around each handler, then delegates to {@link NotificationDispatchService}.
 */
@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationDispatchService notificationDispatchService;

    /**
     * Creates the listener with the dispatch service used for all events.
     *
     * @param notificationDispatchService service that sends and logs notifications
     */
    public NotificationEventListener(NotificationDispatchService notificationDispatchService) {
        this.notificationDispatchService = notificationDispatchService;
    }

    /**
     * Handles payment-completed events by sending a {@code payment-completed} email.
     *
     * @param event payment completed event from Kafka
     */
    @KafkaListener(
            topics = KafkaTopics.PAYMENT_COMPLETED,
            properties = "spring.json.value.default.type=com.cards.common.event.PaymentCompletedEvent"
    )
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        withCorrelation(event.getCorrelationId(), () -> {
            log.info("Received payment.completed paymentId={} userId={}", event.getPaymentId(), event.getUserId());
            Map<String, String> placeholders = new LinkedHashMap<>();
            placeholders.put("paymentId", stringOrEmpty(event.getPaymentId()));
            placeholders.put("accountId", stringOrEmpty(event.getAccountId()));
            placeholders.put("amount", stringOrEmpty(event.getAmount()));
            placeholders.put("currency", event.getCurrency());
            placeholders.put("paymentMethod", event.getPaymentMethod());
            notificationDispatchService.dispatchEmailForPayment(
                    event.getUserId(),
                    "payment-completed",
                    placeholders,
                    event.getCorrelationId()
            );
        });
    }

    /**
     * Handles payment-failed events by sending a {@code payment-failed} email.
     *
     * @param event payment failed event from Kafka
     */
    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            properties = "spring.json.value.default.type=com.cards.common.event.PaymentFailedEvent"
    )
    public void onPaymentFailed(PaymentFailedEvent event) {
        withCorrelation(event.getCorrelationId(), () -> {
            log.info("Received payment.failed paymentId={} userId={}", event.getPaymentId(), event.getUserId());
            Map<String, String> placeholders = new LinkedHashMap<>();
            placeholders.put("paymentId", stringOrEmpty(event.getPaymentId()));
            placeholders.put("accountId", stringOrEmpty(event.getAccountId()));
            placeholders.put("amount", stringOrEmpty(event.getAmount()));
            placeholders.put("currency", event.getCurrency());
            placeholders.put("paymentMethod", event.getPaymentMethod());
            placeholders.put("reason", event.getReason());
            notificationDispatchService.dispatchEmailForPayment(
                    event.getUserId(),
                    "payment-failed",
                    placeholders,
                    event.getCorrelationId()
            );
        });
    }

    /**
     * Handles direct notification-requested events by dispatching them as-is.
     *
     * @param event notification request event from Kafka
     */
    @KafkaListener(
            topics = KafkaTopics.NOTIFICATION_REQUESTED,
            properties = "spring.json.value.default.type=com.cards.common.event.NotificationRequestedEvent"
    )
    public void onNotificationRequested(NotificationRequestedEvent event) {
        withCorrelation(event.getCorrelationId(), () -> {
            log.info(
                    "Received notification.requested notificationId={} channel={} userId={}",
                    event.getNotificationId(),
                    event.getChannel(),
                    event.getUserId()
            );
            notificationDispatchService.dispatch(event);
        });
    }

    /**
     * Runs an action with the correlation id stored in MDC, then clears it.
     *
     * @param correlationId correlation id from the event, or a new UUID when blank
     * @param action        work to run while MDC is set
     */
    private void withCorrelation(String correlationId, Runnable action) {
        String resolved = correlationId != null && !correlationId.isBlank()
                ? correlationId
                : java.util.UUID.randomUUID().toString();
        MDC.put(CorrelationConstants.MDC_CORRELATION_ID, resolved);
        try {
            action.run();
        } finally {
            MDC.remove(CorrelationConstants.MDC_CORRELATION_ID);
        }
    }

    /**
     * Converts a value to string, or empty string when null.
     *
     * @param value any object
     * @return string form or empty string
     */
    private static String stringOrEmpty(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
