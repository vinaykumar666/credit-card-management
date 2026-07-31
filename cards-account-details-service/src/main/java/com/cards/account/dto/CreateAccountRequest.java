package com.cards.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request body for creating a new credit-card account.
 * Fields are validated before the write service persists the account.
 */
@Data
public class CreateAccountRequest {

    /** Owning user identifier (required). */
    @NotNull
    private UUID userId;

    /** Unique account number to assign (required, max 34 chars). */
    @NotBlank
    @Size(max = 34)
    private String accountNumber;

    /** Last four digits of the card (exactly 4 characters). */
    @NotBlank
    @Size(min = 4, max = 4)
    private String cardLastFour;

    /** Card brand name (required). */
    @NotBlank
    @Size(max = 20)
    private String cardBrand;

    /** Credit limit; must be greater than zero. */
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal creditLimit;

    /** ISO currency code (exactly 3 characters). */
    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    /** Account holder name (required). */
    @NotBlank
    @Size(max = 255)
    private String holderName;

    /** Holder email address (required, valid email format). */
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    /** Optional holder phone number. */
    @Size(max = 32)
    private String phone;
}
