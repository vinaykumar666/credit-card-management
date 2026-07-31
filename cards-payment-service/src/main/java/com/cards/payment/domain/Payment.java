package com.cards.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for a payment stored in the {@code payments} table.
 * Holds amount, method, status, and optional external or failure details.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    /** Unique payment identifier. */
    @Id
    private UUID id;

    /** Account charged for this payment. */
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    /** User who initiated the payment. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** Payment amount. */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /** ISO currency code (3 letters). */
    @Column(nullable = false, length = 3)
    private String currency;

    /** Channel used to pay (card, UPI, and so on). */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    /** Current payment status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    /** Reference from an external processor, when available. */
    @Column(name = "external_ref", length = 100)
    private String externalRef;

    /** Why the payment failed, when status is FAILED. */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    /** Request correlation id for tracing across services. */
    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    /** Banking purpose: transfer, bill pay, or card self-payment. */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 30)
    @Builder.Default
    private PaymentType paymentType = PaymentType.CARD_PAYMENT;

    /** Saved beneficiary used for transfer / bill pay, when applicable. */
    @Column(name = "beneficiary_id")
    private UUID beneficiaryId;

    /** Snapshot of payee name at payment time. */
    @Column(name = "beneficiary_name")
    private String beneficiaryName;

    /** Snapshot of payee account number at payment time. */
    @Column(name = "beneficiary_account", length = 34)
    private String beneficiaryAccount;

    /** Snapshot of payee bank name. */
    @Column(name = "bank_name")
    private String bankName;

    /** Snapshot of IFSC / routing code. */
    @Column(name = "ifsc_or_routing", length = 32)
    private String ifscOrRouting;

    /** Customer narration / remarks. */
    @Column(length = 500)
    private String remarks;

    /** Human-friendly bank reference shown on receipts. */
    @Column(name = "reference_number", length = 64)
    private String referenceNumber;

    /** When the payment row was created. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** When the payment row was last updated. */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Assigns an id and timestamps before the entity is first saved.
     */
    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Refreshes the updated-at timestamp before each update.
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
