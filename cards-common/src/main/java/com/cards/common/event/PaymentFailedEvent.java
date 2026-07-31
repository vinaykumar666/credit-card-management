package com.cards.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Kafka event payload published when a payment fails.
 * Carries payment identity, amount, method, failure reason, and correlation id for downstream handlers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {

    /** Unique payment id that failed. */
    private UUID paymentId;
    /** Account charged (or attempted). */
    private UUID accountId;
    /** User who initiated the payment. */
    private UUID userId;
    /** Payment amount. */
    private BigDecimal amount;
    /** Currency code (for example USD). */
    private String currency;
    /** Payment method used. */
    private String paymentMethod;
    /** Why the payment failed. */
    private String reason;
    /** Correlation id for tracing across services. */
    private String correlationId;
    /** When the failure was recorded. */
    private Instant failedAt;
}
