package com.cards.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
 * JPA entity for a ledger line linked to a completed payment.
 * Records the debit (or other entry type) against an account.
 */
@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {

    /** Unique ledger entry identifier. */
    @Id
    private UUID id;

    /** Payment this ledger line belongs to. */
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    /** Account affected by this entry. */
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    /** Kind of entry, for example {@code DEBIT}. */
    @Column(name = "entry_type", nullable = false, length = 20)
    private String entryType;

    /** Entry amount. */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /** ISO currency code (3 letters). */
    @Column(nullable = false, length = 3)
    private String currency;

    /** When the entry was created. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Assigns an id and created-at time before the entity is first saved.
     */
    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
    }
}
