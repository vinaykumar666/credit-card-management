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
 * Bill / merchant payment. Uses a MERCHANT beneficiary when {@code beneficiaryId} is set,
 * otherwise accepts one-time payee details in the request.
 */
@Data
public class MakePaymentRequest {

    @NotNull
    private UUID accountId;

    @NotNull
    private UUID userId;

    /** Preferred: pay a saved merchant beneficiary. */
    private UUID beneficiaryId;

    /** One-time payee name when beneficiaryId is null. */
    @Size(max = 255)
    private String payeeName;

    @Size(max = 34)
    private String payeeAccountNumber;

    @Size(max = 255)
    private String payeeBankName;

    @Size(max = 32)
    private String payeeIfscOrRouting;

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

    /** Optional bill / invoice reference from the merchant. */
    @Size(max = 64)
    private String billReference;
}
