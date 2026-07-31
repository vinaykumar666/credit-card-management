package com.cards.payment.kafka;

import com.cards.common.event.NotificationRequestedEvent;
import com.cards.common.event.PaymentCompletedEvent;
import com.cards.common.event.PaymentFailedEvent;
import com.cards.common.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes payment lifecycle and notification events to Kafka topics.
 */
@Component
public class KafkaPaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaPaymentEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Creates the publisher with the shared Kafka template.
     *
     * @param kafkaTemplate template used to send messages
     */
    public KafkaPaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends a payment-completed event keyed by payment id.
     *
     * @param event completed payment details
     */
    public void publishCompleted(PaymentCompletedEvent event) {
        log.info("Publishing PaymentCompletedEvent paymentId={}", event.getPaymentId());
        kafkaTemplate.send(KafkaTopics.PAYMENT_COMPLETED, event.getPaymentId().toString(), event);
    }

    /**
     * Sends a payment-failed event keyed by payment id.
     *
     * @param event failed payment details
     */
    public void publishFailed(PaymentFailedEvent event) {
        log.info("Publishing PaymentFailedEvent paymentId={}", event.getPaymentId());
        kafkaTemplate.send(KafkaTopics.PAYMENT_FAILED, event.getPaymentId().toString(), event);
    }

    /**
     * Sends a notification-requested event keyed by user id.
     *
     * @param event notification request details
     */
    public void publishNotification(NotificationRequestedEvent event) {
        log.info("Publishing NotificationRequestedEvent notificationId={}", event.getNotificationId());
        kafkaTemplate.send(KafkaTopics.NOTIFICATION_REQUESTED, event.getUserId().toString(), event);
    }
}
