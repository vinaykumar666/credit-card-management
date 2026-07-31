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

    /** Dead-letter suffix appended by the consumer error handler (topic.DLT). */
    public static final String DLT_SUFFIX = ".DLT";

    public static final String PAYMENT_COMPLETED_DLT = PAYMENT_COMPLETED + DLT_SUFFIX;
    public static final String PAYMENT_FAILED_DLT = PAYMENT_FAILED + DLT_SUFFIX;
    public static final String NOTIFICATION_REQUESTED_DLT = NOTIFICATION_REQUESTED + DLT_SUFFIX;

    private KafkaTopics() {
    }
}
