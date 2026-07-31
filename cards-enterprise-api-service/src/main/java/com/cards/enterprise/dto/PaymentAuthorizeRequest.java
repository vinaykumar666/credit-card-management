package com.cards.enterprise.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Platform domain request for enterprise payment authorization.
 * Validated at the API boundary before being adapted to the external network format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAuthorizeRequest {

    /** Platform payment identifier (required). */
    @NotBlank
    private String paymentId;

    /** Token representing the card to charge (required). */
    @NotBlank
    private String cardToken;

    /** Amount to authorize; must be at least 0.01. */
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    /** ISO currency code (required). */
    @NotBlank
    private String currency;

    /** Merchant identifier on the platform (required). */
    @NotBlank
    private String merchantId;
}
