package com.cards.bff.service;

import com.cards.bff.client.DownstreamClient;
import com.cards.bff.web.dto.AccountDto;
import com.cards.bff.web.dto.DashboardResponse;
import com.cards.bff.web.dto.InitiatePaymentRequest;
import com.cards.bff.web.dto.NotificationDto;
import com.cards.bff.web.dto.PaymentDto;
import com.cards.bff.web.dto.TransactionDto;
import com.cards.bff.web.dto.TransactionHistoryDto;
import com.cards.common.channel.ChannelClientContext;
import com.cards.common.channel.ChannelClientHolder;
import com.cards.common.error.DownstreamException;
import com.cards.common.error.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Application service that builds dashboard views and proxies account, notification, and payment calls.
 * Combines several downstream responses into a single dashboard payload for the UI.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int RECENT_TX_PAGE = 0;
    private static final int RECENT_TX_SIZE = 5;
    private static final int MAX_ACCOUNTS_FOR_RECENT_TX = 3;
    private static final int MAX_RECENT_TRANSACTIONS = 15;

    private final DownstreamClient downstreamClient;

    /**
     * Loads accounts, recent transactions, and notifications for the signed-in user.
     *
     * @param userId authenticated user ID
     * @param email  user email from the JWT (may be {@code null})
     * @return aggregated dashboard response including channel and client IDs from the request context
     * @throws DownstreamException when a downstream call fails or an unexpected runtime error occurs
     */
    public DashboardResponse getDashboard(UUID userId, String email) {
        try {
            List<AccountDto> accounts = DownstreamClient.orEmpty(downstreamClient.getAccountsByUser(userId));
            List<NotificationDto> notifications =
                    DownstreamClient.orEmpty(downstreamClient.getNotificationsByUser(userId));
            List<TransactionDto> recentTransactions = loadRecentTransactions(accounts);

            ChannelClientContext ctx = ChannelClientHolder.get();
            String channelId = ctx != null ? ctx.channelId() : "";
            String clientId = ctx != null ? ctx.clientId() : "";

            return new DashboardResponse(
                    userId,
                    email,
                    accounts,
                    recentTransactions,
                    notifications,
                    channelId,
                    clientId
            );
        } catch (DownstreamException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new DownstreamException(ErrorCodes.BFF_005, ex);
        }
    }

    /**
     * Returns accounts for the given user.
     *
     * @param userId user ID
     * @return non-null list of accounts (empty if none)
     */
    public List<AccountDto> getAccounts(UUID userId) {
        return DownstreamClient.orEmpty(downstreamClient.getAccountsByUser(userId));
    }

    /**
     * Returns a page of transactions for an account.
     *
     * @param accountId account ID
     * @param page      zero-based page index
     * @param size      page size
     * @return paged transaction history from the accounts service
     */
    public TransactionHistoryDto getTransactions(UUID accountId, int page, int size) {
        return downstreamClient.getTransactions(accountId, page, size);
    }

    /**
     * Returns notifications for the given user.
     *
     * @param userId user ID
     * @return non-null list of notifications (empty if none)
     */
    public List<NotificationDto> getNotifications(UUID userId) {
        return DownstreamClient.orEmpty(downstreamClient.getNotificationsByUser(userId));
    }

    /**
     * Initiates a payment through the payments service.
     *
     * @param request payment details
     * @return payment result from downstream
     */
    public PaymentDto initiatePayment(InitiatePaymentRequest request) {
        return downstreamClient.initiatePayment(request);
    }

    /**
     * Loads recent transactions from up to a few accounts and returns the newest ones.
     *
     * @param accounts user accounts to sample
     * @return recent transactions sorted newest-first, capped in size
     */
    private List<TransactionDto> loadRecentTransactions(List<AccountDto> accounts) {
        List<TransactionDto> collected = new ArrayList<>();
        accounts.stream()
                .limit(MAX_ACCOUNTS_FOR_RECENT_TX)
                .forEach(account -> {
                    TransactionHistoryDto history =
                            downstreamClient.getTransactions(account.id(), RECENT_TX_PAGE, RECENT_TX_SIZE);
                    if (history != null && history.transactions() != null) {
                        collected.addAll(history.transactions());
                    }
                });
        return collected.stream()
                .sorted(Comparator.comparing(TransactionDto::occurredAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(MAX_RECENT_TRANSACTIONS)
                .toList();
    }
}
