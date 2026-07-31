package com.cards.bff.web.dto;

import java.util.List;
import java.util.UUID;

/**
 * Aggregated dashboard payload for the signed-in user.
 * Combines accounts, recent transactions, notifications, and the request tenant identifiers.
 *
 * @param userId             authenticated user ID
 * @param email              user email from the JWT when available
 * @param accounts           user credit accounts
 * @param recentTransactions recent transactions across a few accounts
 * @param notifications      user notifications
 * @param channelId          channel ID from the current request context
 * @param clientId           client ID from the current request context
 */
public record DashboardResponse(
        UUID userId,
        String email,
        List<AccountDto> accounts,
        List<TransactionDto> recentTransactions,
        List<NotificationDto> notifications,
        String channelId,
        String clientId
) {
}
