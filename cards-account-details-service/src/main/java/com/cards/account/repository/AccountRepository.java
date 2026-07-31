package com.cards.account.repository;

import com.cards.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for {@link Account} persistence.
 * Provides lookups by user and account number on top of standard CRUD.
 */
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Finds all accounts owned by the given user.
     *
     * @param userId user identifier
     * @return list of matching accounts (may be empty)
     */
    List<Account> findByUserId(UUID userId);

    /**
     * Finds an account by its unique account number.
     *
     * @param accountNumber account number to look up
     * @return the account if found, otherwise empty
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Checks whether an account number is already in use.
     *
     * @param accountNumber account number to check
     * @return {@code true} if an account with that number exists
     */
    boolean existsByAccountNumber(String accountNumber);
}
