package com.cards.bff.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Payment details returned after create or lookup through the BFF.
 *
 * @param id             payment identifier
 * @param accountId      related account
 * @param userId         owning user
 * @param amount         payment amount
 * @param currency       ISO currency code
 * @param paymentMethod  payment method used
 * @param status         current payment status
 * @param externalRef    external processor reference when present
 * @param failureReason  reason when the payment failed
 * @param correlationId  correlation ID for tracing
 * @param createdAt      creation timestamp
 * @param updatedAt      last update timestamp
 */
public record PaymentDto(
        UUID id,
        UUID accountId,
        UUID userId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String status,
        String externalRef,
        String failureReason,
        String correlationId,
        Instant createdAt,
        Instant updatedAt
) {
}
