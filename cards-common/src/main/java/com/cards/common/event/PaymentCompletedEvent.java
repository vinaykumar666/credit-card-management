package com.cards.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Kafka event payload published when a payment completes successfully.
 * Carries payment identity, amount, method, and correlation id for downstream handlers (for example notifications).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {

    /** Unique payment id that completed. */
    private UUID paymentId;
    /** Account that was charged. */
    private UUID accountId;
    /** User who initiated the payment. */
    private UUID userId;
    /** Payment amount. */
    private BigDecimal amount;
    /** Currency code (for example USD). */
    private String currency;
    /** Payment method used. */
    private String paymentMethod;
    /** Correlation id for tracing across services. */
    private String correlationId;
    /** When the payment completed. */
    private Instant completedAt;
}
