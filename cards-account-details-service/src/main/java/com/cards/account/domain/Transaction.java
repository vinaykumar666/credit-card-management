package com.cards.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
 * JPA entity for a single account transaction stored in the {@code transactions} table.
 * Links to an {@link Account} and records amount, merchant, status, and timing.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /** Unique transaction identifier. */
    @Id
    private UUID id;

    /** Account this transaction belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** Transaction type (for example, PURCHASE or PAYMENT). */
    @Column(nullable = false, length = 30)
    private String type;

    /** Transaction amount. */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /** ISO currency code for the amount. */
    @Column(nullable = false, length = 3)
    private String currency;

    /** Optional merchant name. */
    @Column(length = 255)
    private String merchant;

    /** Optional free-text description. */
    @Column(length = 500)
    private String description;

    /** Processing status of the transaction. */
    @Column(nullable = false, length = 20)
    private String status;

    /** When the transaction occurred (business time). */
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    /** When the row was created in the database. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Assigns an id and created timestamp before the first persist when missing.
     */
    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
