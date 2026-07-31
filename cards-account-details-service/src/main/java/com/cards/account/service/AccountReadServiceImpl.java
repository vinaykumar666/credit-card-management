package com.cards.account.service;

import com.cards.account.domain.Account;
import com.cards.account.dto.AccountResponse;
import com.cards.account.dto.TransactionHistoryResponse;
import com.cards.account.mapper.AccountMapper;
import com.cards.account.mapper.TransactionMapper;
import com.cards.account.repository.AccountRepository;
import com.cards.account.repository.TransactionRepository;
import com.cards.common.error.ErrorCodes;
import com.cards.common.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Default read-only implementation of {@link AccountReadService}.
 * Loads accounts and paged transactions from the database and maps them to DTOs.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountReadServiceImpl implements AccountReadService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountResponse getById(UUID id) {
        return accountMapper.toResponse(findAccount(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AccountResponse> getByUserId(UUID userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionHistoryResponse getTransactions(UUID accountId, int page, int size) {
        findAccount(accountId);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));
        var result = transactionRepository.findByAccountId(accountId, pageable);
        return TransactionHistoryResponse.builder()
                .accountId(accountId)
                .transactions(result.getContent().stream().map(transactionMapper::toResponse).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    /**
     * Loads an account or fails with a not-found business error.
     *
     * @param id account identifier
     * @return the account entity
     * @throws NotFoundException if no account exists for the id
     */
    private Account findAccount(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.ACCT_001));
    }
}
