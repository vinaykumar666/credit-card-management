package com.cards.common.eventstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

/**
 * JDBC implementation of {@link AppEventStore} writing into {@code app_event}.
 * Failures are logged and swallowed so audit writes never break business flows.
 */
public class JdbcAppEventStore implements AppEventStore {

    private static final Logger log = LoggerFactory.getLogger(JdbcAppEventStore.class);

    private final JdbcTemplate jdbcTemplate;

    public JdbcAppEventStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void record(AppEventRecord event) {
        try {
            jdbcTemplate.update("""
                            INSERT INTO app_event (
                              id, event_name, event_phase, service_name, method_name,
                              user_id, user_name, amount, transaction_id, correlation_id,
                              channel_id, client_id, http_method, path, status, duration_ms, details
                            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                            """,
                    UUID.randomUUID(),
                    event.eventName(),
                    event.eventPhase(),
                    event.serviceName(),
                    event.methodName(),
                    event.userId(),
                    truncate(event.userName(), 255),
                    event.amount(),
                    truncate(event.transactionId(), 64),
                    truncate(event.correlationId(), 64),
                    truncate(event.channelId(), 64),
                    truncate(event.clientId(), 64),
                    truncate(event.httpMethod(), 16),
                    truncate(event.path(), 512),
                    truncate(event.status(), 40),
                    event.durationMs(),
                    truncate(event.details(), 2000)
            );
        } catch (Exception ex) {
            log.warn("Failed to persist app_event name={} phase={}: {}",
                    event.eventName(), event.eventPhase(), ex.getMessage());
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
