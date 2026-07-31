package com.cards.account.repository;

import com.cards.account.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data repository for {@link Transaction} persistence.
 * Supports paged retrieval of transactions for a given account.
 */
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Returns a page of transactions for the given account.
     *
     * @param accountId account whose transactions are requested
     * @param pageable  paging and sorting options
     * @return page of matching transactions
     */
    Page<Transaction> findByAccountId(UUID accountId, Pageable pageable);
}
