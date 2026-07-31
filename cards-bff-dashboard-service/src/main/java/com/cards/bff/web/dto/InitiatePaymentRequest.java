package com.cards.bff.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request body for starting a payment through the BFF.
 * The controller overwrites {@code userId} with the authenticated JWT subject before calling downstream.
 *
 * @param accountId     account to charge or pay from
 * @param userId        user ID (replaced from JWT in the controller)
 * @param amount        payment amount (minimum 0.01)
 * @param currency      three-letter ISO currency code
 * @param paymentMethod how the payment is made (for example card or ACH)
 */
public record InitiatePaymentRequest(
        @NotNull UUID accountId,
        @NotNull UUID userId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotBlank String paymentMethod
) {
}
