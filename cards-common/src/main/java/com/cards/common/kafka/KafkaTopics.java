package com.cards.common.kafka;

/**
 * Canonical Kafka topic names shared by producers and consumers across services.
 * Keeps topic strings in one place to avoid typos and drift.
 */
public final class KafkaTopics {

    /** Topic for successful payment completion events. */
    public static final String PAYMENT_COMPLETED = "payment.completed";
    /** Topic for payment failure events. */
    public static final String PAYMENT_FAILED = "payment.failed";
    /** Topic for outbound notification requests. */
    public static final String NOTIFICATION_REQUESTED = "notification.requested";

    private KafkaTopics() {
    }
}
