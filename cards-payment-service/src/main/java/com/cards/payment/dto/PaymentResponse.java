package com.cards.payment.dto;

import com.cards.payment.domain.PaymentMethod;
import com.cards.payment.domain.PaymentStatus;
import com.cards.payment.domain.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * API response describing a payment / transfer and its current state.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private UUID id;
    private UUID accountId;
    private UUID userId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentType paymentType;
    private PaymentStatus status;
    private UUID beneficiaryId;
    private String beneficiaryName;
    private String beneficiaryAccount;
    private String bankName;
    private String ifscOrRouting;
    private String remarks;
    private String referenceNumber;
    private String externalRef;
    private String failureReason;
    private String correlationId;
    private Instant createdAt;
    private Instant updatedAt;
}
