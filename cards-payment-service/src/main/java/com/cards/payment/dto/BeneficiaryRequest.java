package com.cards.payment.dto;

import com.cards.payment.domain.BeneficiaryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * Create / update payload for a beneficiary (payee).
 */
@Data
public class BeneficiaryRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    @Size(max = 100)
    private String nickname;

    @NotBlank
    @Size(max = 255)
    private String beneficiaryName;

    @NotBlank
    @Size(max = 34)
    private String accountNumber;

    @NotBlank
    @Size(max = 255)
    private String bankName;

    @NotBlank
    @Size(max = 32)
    private String ifscOrRouting;

    @NotNull
    private BeneficiaryType beneficiaryType;
}
