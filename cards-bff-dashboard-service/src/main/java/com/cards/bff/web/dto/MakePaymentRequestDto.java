package com.cards.bff.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record MakePaymentRequestDto(
        @NotNull UUID accountId,
        UUID beneficiaryId,
        @Size(max = 255) String payeeName,
        @Size(max = 34) String payeeAccountNumber,
        @Size(max = 255) String payeeBankName,
        @Size(max = 32) String payeeIfscOrRouting,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotBlank String paymentMethod,
        @Size(max = 500) String remarks,
        @Size(max = 64) String billReference
) {
}
