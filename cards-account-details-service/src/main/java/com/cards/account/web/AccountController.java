package com.cards.account.web;

import com.cards.account.dto.AccountResponse;
import com.cards.account.dto.CreateAccountRequest;
import com.cards.account.dto.TransactionHistoryResponse;
import com.cards.account.service.AccountReadService;
import com.cards.account.service.AccountWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for account lookup, creation, and transaction history.
 * Exposes {@code /api/v1/accounts} endpoints backed by read and write services.
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountReadService accountReadService;
    private final AccountWriteService accountWriteService;

    /**
     * Returns one account by id.
     *
     * @param id account identifier
     * @return account details
     */
    @GetMapping("/{id}")
    public AccountResponse getById(@PathVariable UUID id) {
        return accountReadService.getById(id);
    }

    /**
     * Returns all accounts for a user.
     *
     * @param userId user identifier
     * @return list of accounts (may be empty)
     */
    @GetMapping("/user/{userId}")
    public List<AccountResponse> getByUserId(@PathVariable UUID userId) {
        return accountReadService.getByUserId(userId);
    }

    /**
     * Creates a new account.
     *
     * @param request validated create-account body
     * @return the created account
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        return accountWriteService.createAccount(request);
    }

    /**
     * Returns a page of transactions for an account.
     *
     * @param id   account identifier
     * @param page zero-based page index (default 0)
     * @param size page size (default 20)
     * @return paged transaction history
     */
    @GetMapping("/{id}/transactions")
    public TransactionHistoryResponse getTransactions(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return accountReadService.getTransactions(id, page, size);
    }
}
