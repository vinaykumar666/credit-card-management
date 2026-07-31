package com.cards.notification.kafka;

import com.cards.common.correlation.CorrelationConstants;
import com.cards.common.event.NotificationRequestedEvent;
import com.cards.common.event.PaymentCompletedEvent;
import com.cards.common.event.PaymentFailedEvent;
import com.cards.common.eventstore.AppEventNames;
import com.cards.common.eventstore.AppEventRecord;
import com.cards.common.eventstore.AppEventStore;
import com.cards.common.kafka.KafkaTopics;
import com.cards.common.logging.LifecycleLog;
import com.cards.notification.service.KafkaIdempotencyService;
import com.cards.notification.service.NotificationDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kafka listeners with manual ack, idempotency, and START/END lifecycle logs.
 */
@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationDispatchService notificationDispatchService;
    private final KafkaIdempotencyService kafkaIdempotencyService;
    private final ObjectProvider<AppEventStore> appEventStore;

    public NotificationEventListener(
            NotificationDispatchService notificationDispatchService,
            KafkaIdempotencyService kafkaIdempotencyService,
            ObjectProvider<AppEventStore> appEventStore
    ) {
        this.notificationDispatchService = notificationDispatchService;
        this.kafkaIdempotencyService = kafkaIdempotencyService;
        this.appEventStore = appEventStore;
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_COMPLETED,
            properties = "spring.json.value.default.type=com.cards.common.event.PaymentCompletedEvent"
    )
    public void onPaymentCompleted(PaymentCompletedEvent event, Acknowledgment acknowledgment) {
        Map<String, Object> fields = LifecycleLog.ctx(
                "userId", event.getUserId(),
                "amount", event.getAmount(),
                "transactionId", event.getPaymentId()
        );
        runWithLifecycle("onPaymentCompleted", fields, acknowledgment, KafkaTopics.PAYMENT_COMPLETED,
                KafkaTopics.PAYMENT_COMPLETED + ":" + event.getPaymentId(),
                () -> withCorrelation(event.getCorrelationId(), () -> {
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
                }));
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            properties = "spring.json.value.default.type=com.cards.common.event.PaymentFailedEvent"
    )
    public void onPaymentFailed(PaymentFailedEvent event, Acknowledgment acknowledgment) {
        Map<String, Object> fields = LifecycleLog.ctx(
                "userId", event.getUserId(),
                "amount", event.getAmount(),
                "transactionId", event.getPaymentId()
        );
        runWithLifecycle("onPaymentFailed", fields, acknowledgment, KafkaTopics.PAYMENT_FAILED,
                KafkaTopics.PAYMENT_FAILED + ":" + event.getPaymentId(),
                () -> withCorrelation(event.getCorrelationId(), () -> {
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
                }));
    }

    @KafkaListener(
            topics = KafkaTopics.NOTIFICATION_REQUESTED,
            properties = "spring.json.value.default.type=com.cards.common.event.NotificationRequestedEvent"
    )
    public void onNotificationRequested(NotificationRequestedEvent event, Acknowledgment acknowledgment) {
        Map<String, Object> fields = LifecycleLog.ctx(
                "userId", event.getUserId(),
                "transactionId", event.getNotificationId()
        );
        runWithLifecycle("onNotificationRequested", fields, acknowledgment, KafkaTopics.NOTIFICATION_REQUESTED,
                KafkaTopics.NOTIFICATION_REQUESTED + ":" + event.getNotificationId(),
                () -> withCorrelation(event.getCorrelationId(), () -> {
                    log.info(
                            "Received notification.requested notificationId={} channel={} userId={}",
                            event.getNotificationId(),
                            event.getChannel(),
                            event.getUserId()
                    );
                    notificationDispatchService.dispatch(event);
                }));
    }

    private void runWithLifecycle(
            String method,
            Map<String, Object> fields,
            Acknowledgment acknowledgment,
            String topic,
            String eventKey,
            Runnable action
    ) {
        long started = System.currentTimeMillis();
        LifecycleLog.bind(fields.get("userId"), null, fields.get("amount"), fields.get("transactionId"));
        LifecycleLog.start(method, fields);
        try {
            processOnce(eventKey, topic, acknowledgment, action);
            LifecycleLog.end(method, fields, System.currentTimeMillis() - started);
        } catch (RuntimeException ex) {
            LifecycleLog.fail(method, fields, System.currentTimeMillis() - started, ex);
            throw ex;
        } finally {
            LifecycleLog.clearBusinessContext();
        }
    }

    private void processOnce(String eventKey, String topic, Acknowledgment acknowledgment, Runnable action) {
        if (!kafkaIdempotencyService.claim(eventKey, topic)) {
            log.info("Skipping already-processed kafka event key={}", eventKey);
            acknowledgment.acknowledge();
            return;
        }
        try {
            action.run();
            recordConsumed(eventKey, topic);
            acknowledgment.acknowledge();
        } catch (RuntimeException ex) {
            kafkaIdempotencyService.release(eventKey);
            throw ex;
        }
    }

    private void recordConsumed(String eventKey, String topic) {
        AppEventStore store = appEventStore.getIfAvailable();
        if (store == null) {
            return;
        }
        store.record(new AppEventRecord(
                AppEventNames.KAFKA_CONSUMED,
                "CONSUMED",
                "cards-notification-service",
                null,
                null,
                null,
                null,
                eventKey,
                MDC.get(CorrelationConstants.MDC_CORRELATION_ID),
                null,
                null,
                null,
                topic,
                "OK",
                null,
                eventKey
        ));
    }

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

    private static String stringOrEmpty(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
