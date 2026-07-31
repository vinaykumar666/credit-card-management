package com.cards.account.service;

import com.cards.account.dto.AccountResponse;
import com.cards.account.dto.CreateAccountRequest;

/**
 * Write-side contract for account mutations.
 * Implementations create or change account records in a transactional way.
 */
public interface AccountWriteService {

    /**
     * Creates a new account from the given request.
     *
     * @param request validated create-account payload
     * @return the created account as an API response
     * @throws com.cards.common.error.ConflictException if the account number already exists
     */
    AccountResponse createAccount(CreateAccountRequest request);
}
