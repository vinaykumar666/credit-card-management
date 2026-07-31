package com.cards.account.service;

import com.cards.account.dto.AccountResponse;
import com.cards.account.dto.TransactionHistoryResponse;

import java.util.List;
import java.util.UUID;

/**
 * Read-side contract for account and transaction queries.
 * Implementations load data without modifying account state.
 */
public interface AccountReadService {

    /**
     * Loads a single account by id.
     *
     * @param id account identifier
     * @return account details
     * @throws com.cards.common.error.NotFoundException if the account does not exist
     */
    AccountResponse getById(UUID id);

    /**
     * Lists all accounts for a user.
     *
     * @param userId user identifier
     * @return accounts owned by the user (may be empty)
     */
    List<AccountResponse> getByUserId(UUID userId);

    /**
     * Returns a page of transactions for an account, newest first.
     *
     * @param accountId account identifier
     * @param page      zero-based page index
     * @param size      page size
     * @return paged transaction history
     * @throws com.cards.common.error.NotFoundException if the account does not exist
     */
    TransactionHistoryResponse getTransactions(UUID accountId, int page, int size);
}
