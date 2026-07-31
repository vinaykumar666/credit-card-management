package com.cards.payment.dto;

import com.cards.payment.domain.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request body for starting a new payment.
 * Carries the account, user, amount, currency, and payment method.
 */
@Data
public class InitiatePaymentRequest {

    /** Account to charge. */
    @NotNull
    private UUID accountId;

    /** User starting the payment. */
    @NotNull
    private UUID userId;

    /** Amount to pay; must be greater than zero. */
    @NotNull
    @DecimalMin(value = "0.01", message = "amount must be greater than zero")
    private BigDecimal amount;

    /** ISO currency code (exactly 3 letters). */
    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    /** How the customer will pay. */
    @NotNull
    private PaymentMethod paymentMethod;
}
