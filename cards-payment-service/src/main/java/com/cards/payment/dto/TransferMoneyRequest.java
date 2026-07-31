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
 * Transfer money from the customer's card/account to a saved beneficiary.
 */
@Data
public class TransferMoneyRequest {

    @NotNull
    private UUID accountId;

    @NotNull
    private UUID userId;

    @NotNull
    private UUID beneficiaryId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    private PaymentMethod paymentMethod;

    @Size(max = 500)
    private String remarks;
}
