package com.cards.bff.web.dto;

import java.util.List;
import java.util.UUID;

/**
 * Paged transaction history for one account.
 *
 * @param accountId     account these transactions belong to
 * @param transactions  page of transactions
 * @param page          zero-based page index
 * @param size          page size
 * @param totalElements total matching transactions
 * @param totalPages    total number of pages
 */
public record TransactionHistoryDto(
        UUID accountId,
        List<TransactionDto> transactions,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
