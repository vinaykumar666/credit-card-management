package com.cards.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * JPA entity for a credit-card account stored in the {@code accounts} table.
 * Holds card details, credit limits, holder contact info, and lifecycle timestamps.
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    /** Unique account identifier. */
    @Id
    private UUID id;

    /** Owning user identifier. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** Unique account number (up to 34 characters). */
    @Column(name = "account_number", nullable = false, unique = true, length = 34)
    private String accountNumber;

    /** Last four digits of the linked card. */
    @Column(name = "card_last_four", nullable = false, length = 4)
    private String cardLastFour;

    /** Card brand name (for example, VISA or MASTERCARD). */
    @Column(name = "card_brand", nullable = false, length = 20)
    private String cardBrand;

    /** Maximum credit allowed on the account. */
    @Column(name = "credit_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal creditLimit;

    /** Credit still available to spend. */
    @Column(name = "available_credit", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableCredit;

    /** ISO currency code (defaults to USD on create). */
    @Column(nullable = false, length = 3)
    private String currency;

    /** Account status such as ACTIVE. */
    @Column(nullable = false, length = 20)
    private String status;

    /** Name of the account holder. */
    @Column(name = "holder_name", nullable = false)
    private String holderName;

    /** Contact email for the account holder. */
    @Column(nullable = false)
    private String email;

    /** Optional contact phone number. */
    @Column(length = 32)
    private String phone;

    /** When the account row was created. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** When the account row was last updated. */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Sets id, default currency, and timestamps before the first persist.
     */
    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (currency == null) {
            currency = "USD";
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Refreshes {@code updatedAt} before every update.
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
