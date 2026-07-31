package com.cards.bff.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Single account transaction shown on the dashboard or history views.
 *
 * @param id          transaction identifier
 * @param accountId   related account
 * @param type        transaction type
 * @param amount      transaction amount
 * @param currency    ISO currency code
 * @param merchant    merchant name when available
 * @param description free-text description
 * @param status      transaction status
 * @param occurredAt  when the transaction happened
 * @param createdAt   when the record was stored
 */
public record TransactionDto(
        UUID id,
        UUID accountId,
        String type,
        BigDecimal amount,
        String currency,
        String merchant,
        String description,
        String status,
        Instant occurredAt,
        Instant createdAt
) {
}
