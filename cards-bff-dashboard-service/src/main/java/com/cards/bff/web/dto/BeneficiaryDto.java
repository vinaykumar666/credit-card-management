package com.cards.bff.web.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Saved beneficiary (payee) as seen by the UI through the BFF.
 */
public record BeneficiaryDto(
        UUID id,
        UUID userId,
        String nickname,
        String beneficiaryName,
        String accountNumber,
        String bankName,
        String ifscOrRouting,
        String beneficiaryType,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
