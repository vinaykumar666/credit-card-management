package com.cards.bff.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferMoneyRequestDto(
        @NotNull UUID accountId,
        @NotNull UUID beneficiaryId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotBlank String paymentMethod,
        @Size(max = 500) String remarks
) {
}
