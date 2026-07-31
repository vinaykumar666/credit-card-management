package com.cards.payment.repository;

import com.cards.payment.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data repository for persisting and loading {@link LedgerEntry} entities.
 */
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
}
