package com.cards.common.eventstore;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Immutable row written to the {@code app_event} footfall table.
 */
public record AppEventRecord(
        String eventName,
        String eventPhase,
        String serviceName,
        String methodName,
        UUID userId,
        String userName,
        BigDecimal amount,
        String transactionId,
        String correlationId,
        String channelId,
        String clientId,
        String httpMethod,
        String path,
        String status,
        Long durationMs,
        String details
) {
}
