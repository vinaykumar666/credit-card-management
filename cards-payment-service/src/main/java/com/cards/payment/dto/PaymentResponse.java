package com.cards.payment.dto;

import com.cards.payment.domain.PaymentMethod;
import com.cards.payment.domain.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * API response describing a payment and its current state.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    /** Payment identifier. */
    private UUID id;
    /** Account charged for the payment. */
    private UUID accountId;
    /** User who initiated the payment. */
    private UUID userId;
    /** Payment amount. */
    private BigDecimal amount;
    /** ISO currency code. */
    private String currency;
    /** Payment method used. */
    private PaymentMethod paymentMethod;
    /** Current status (pending, completed, or failed). */
    private PaymentStatus status;
    /** External processor reference, if any. */
    private String externalRef;
    /** Failure reason when the payment failed. */
    private String failureReason;
    /** Correlation id used for request tracing. */
    private String correlationId;
    /** When the payment was created. */
    private Instant createdAt;
    /** When the payment was last updated. */
    private Instant updatedAt;
}
