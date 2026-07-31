package com.cards.account.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable API view of a single account transaction.
 * Used inside transaction history responses.
 */
@Value
@Builder
public class TransactionResponse {

    /** Unique transaction identifier. */
    UUID id;
    /** Account the transaction belongs to. */
    UUID accountId;
    /** Transaction type. */
    String type;
    /** Transaction amount. */
    BigDecimal amount;
    /** ISO currency code. */
    String currency;
    /** Optional merchant name. */
    String merchant;
    /** Optional description. */
    String description;
    /** Processing status. */
    String status;
    /** When the transaction occurred. */
    Instant occurredAt;
    /** When the record was stored. */
    Instant createdAt;
}
