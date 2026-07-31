package com.cards.payment.dto;

import com.cards.payment.domain.BeneficiaryStatus;
import com.cards.payment.domain.BeneficiaryType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class BeneficiaryResponse {
    UUID id;
    UUID userId;
    String nickname;
    String beneficiaryName;
    String accountNumber;
    String bankName;
    String ifscOrRouting;
    BeneficiaryType beneficiaryType;
    BeneficiaryStatus status;
    Instant createdAt;
    Instant updatedAt;
}
