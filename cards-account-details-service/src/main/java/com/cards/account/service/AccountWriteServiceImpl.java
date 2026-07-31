package com.cards.account.service;

import com.cards.account.dto.AccountResponse;
import com.cards.account.dto.CreateAccountRequest;
import com.cards.account.mapper.AccountMapper;
import com.cards.account.repository.AccountRepository;
import com.cards.common.error.ConflictException;
import com.cards.common.error.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default transactional implementation of {@link AccountWriteService}.
 * Rejects duplicate account numbers, then maps, saves, and returns the new account.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AccountWriteServiceImpl implements AccountWriteService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new ConflictException(ErrorCodes.ACCT_003);
        }
        var account = accountMapper.toEntity(request);
        return accountMapper.toResponse(accountRepository.save(account));
    }
}
