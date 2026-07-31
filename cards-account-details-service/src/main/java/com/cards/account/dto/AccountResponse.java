package com.cards.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * API response body describing a credit-card account.
 * Returned by account read and create endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    /** Unique account identifier. */
    private UUID id;
    /** Owning user identifier. */
    private UUID userId;
    /** Unique account number. */
    private String accountNumber;
    /** Last four digits of the linked card. */
    private String cardLastFour;
    /** Card brand name. */
    private String cardBrand;
    /** Maximum credit allowed. */
    private BigDecimal creditLimit;
    /** Credit still available. */
    private BigDecimal availableCredit;
    /** ISO currency code. */
    private String currency;
    /** Account status. */
    private String status;
    /** Account holder name. */
    private String holderName;
    /** Holder email address. */
    private String email;
    /** Optional holder phone number. */
    private String phone;
    /** When the account was created. */
    private Instant createdAt;
    /** When the account was last updated. */
    private Instant updatedAt;
}
