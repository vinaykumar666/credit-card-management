package com.cards.bff.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Credit account summary returned by the BFF to UI clients.
 *
 * @param id              account identifier
 * @param userId          owning user identifier
 * @param accountNumber   account number
 * @param cardLastFour    last four digits of the card
 * @param cardBrand       card brand (for example Visa or Mastercard)
 * @param creditLimit     total credit limit
 * @param availableCredit remaining available credit
 * @param currency        ISO currency code
 * @param status          account status
 * @param holderName      cardholder name
 * @param email           contact email
 * @param phone           contact phone
 * @param createdAt       creation timestamp
 * @param updatedAt       last update timestamp
 */
public record AccountDto(
        UUID id,
        UUID userId,
        String accountNumber,
        String cardLastFour,
        String cardBrand,
        BigDecimal creditLimit,
        BigDecimal availableCredit,
        String currency,
        String status,
        String holderName,
        String email,
        String phone,
        Instant createdAt,
        Instant updatedAt
) {
}
