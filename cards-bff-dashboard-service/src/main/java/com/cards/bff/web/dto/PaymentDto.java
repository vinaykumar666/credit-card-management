package com.cards.bff.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Payment / transfer details returned to the UI.
 */
public record PaymentDto(
        UUID id,
        UUID accountId,
        UUID userId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String paymentType,
        String status,
        UUID beneficiaryId,
        String beneficiaryName,
        String beneficiaryAccount,
        String bankName,
        String ifscOrRouting,
        String remarks,
        String referenceNumber,
        String externalRef,
        String failureReason,
        String correlationId,
        Instant createdAt,
        Instant updatedAt
) {
}
