package com.cards.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures each Kafka business key is processed at most once (idempotent consumers).
 */
@Service
public class KafkaIdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(KafkaIdempotencyService.class);

    private final JdbcTemplate jdbcTemplate;

    public KafkaIdempotencyService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Claims a key for processing. Returns {@code false} if already processed.
     * On handler failure, call {@link #release(String)} so a retry can reclaim the key.
     */
    @Transactional
    public boolean claim(String eventKey, String topic) {
        try {
            int rows = jdbcTemplate.update(
                    "INSERT INTO processed_kafka_event (event_key, topic) VALUES (?, ?) ON CONFLICT (event_key) DO NOTHING",
                    eventKey,
                    topic
            );
            return rows == 1;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }

    @Transactional
    public void release(String eventKey) {
        try {
            jdbcTemplate.update("DELETE FROM processed_kafka_event WHERE event_key = ?", eventKey);
        } catch (Exception ex) {
            log.warn("Failed to release kafka idempotency key={}: {}", eventKey, ex.getMessage());
        }
    }
}
