package com.cards.notification.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Production-oriented Kafka consumer error handling: retries then publish to {@code topic.DLT}.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${cards.kafka.dlt.retry-interval-ms:1000}")
    private long retryIntervalMs;

    @Value("${cards.kafka.dlt.max-attempts:3}")
    private long maxAttempts;

    /**
     * After {@code max-attempts} failures, the record is published to {@code <topic>.DLT}
     * and the original offset is committed so the poison pill does not block the partition.
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<?, ?> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );
        // FixedBackOff attempts = maxFailures; interval between retries.
        return new DefaultErrorHandler(recoverer, new FixedBackOff(retryIntervalMs, maxAttempts));
    }
}
