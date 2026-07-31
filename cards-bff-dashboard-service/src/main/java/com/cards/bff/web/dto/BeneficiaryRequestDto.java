package com.cards.bff.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BeneficiaryRequestDto(
        UUID userId,
        @NotBlank @Size(max = 100) String nickname,
        @NotBlank @Size(max = 255) String beneficiaryName,
        @NotBlank @Size(max = 34) String accountNumber,
        @NotBlank @Size(max = 255) String bankName,
        @NotBlank @Size(max = 32) String ifscOrRouting,
        @NotBlank String beneficiaryType
) {
}
