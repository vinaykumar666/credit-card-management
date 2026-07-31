package com.cards.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Paged API response for an account's transaction history.
 * Includes the current page of transactions plus paging metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponse {

    /** Account whose history is returned. */
    private UUID accountId;
    /** Transactions on the current page. */
    private List<TransactionResponse> transactions;
    /** Zero-based page index. */
    private int page;
    /** Page size requested. */
    private int size;
    /** Total number of transactions across all pages. */
    private long totalElements;
    /** Total number of pages. */
    private int totalPages;
}
