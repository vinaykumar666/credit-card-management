package com.cards.bff.web;

import com.cards.bff.service.DashboardService;
import com.cards.bff.web.dto.AccountDto;
import com.cards.bff.web.dto.DashboardResponse;
import com.cards.bff.web.dto.InitiatePaymentRequest;
import com.cards.bff.web.dto.NotificationDto;
import com.cards.bff.web.dto.PaymentDto;
import com.cards.bff.web.dto.TransactionHistoryDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
 * REST API for the dashboard BFF under {@code /bff/v1}.
 * Exposes aggregated dashboard data and thin proxies for accounts, transactions, notifications, and payments.
 */
@RestController
@RequestMapping("/bff/v1")
@RequiredArgsConstructor
public class BffController {

    private final DashboardService dashboardService;

    /**
     * Returns the aggregated dashboard for the authenticated user.
     *
     * @param jwt JWT of the signed-in user
     * @return dashboard payload with accounts, recent transactions, and notifications
     */
    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@AuthenticationPrincipal Jwt jwt) {
        return dashboardService.getDashboard(userId(jwt), jwt.getClaimAsString("email"));
    }

    /**
     * Lists credit accounts for the authenticated user.
     *
     * @param jwt JWT of the signed-in user
     * @return list of accounts
     */
    @GetMapping("/accounts")
    public List<AccountDto> accounts(@AuthenticationPrincipal Jwt jwt) {
        return dashboardService.getAccounts(userId(jwt));
    }

    /**
     * Returns a page of transactions for the given account.
     *
     * @param id   account ID
     * @param page zero-based page index (default 0)
     * @param size page size (default 20)
     * @return paged transaction history
     */
    @GetMapping("/accounts/{id}/transactions")
    public TransactionHistoryDto transactions(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return dashboardService.getTransactions(id, page, size);
    }

    /**
     * Lists notifications for the authenticated user.
     *
     * @param jwt JWT of the signed-in user
     * @return list of notifications
     */
    @GetMapping("/notifications")
    public List<NotificationDto> notifications(@AuthenticationPrincipal Jwt jwt) {
        return dashboardService.getNotifications(userId(jwt));
    }

    /**
     * Initiates a payment for the authenticated user.
     * The JWT subject is used as {@code userId} in the request sent downstream.
     *
     * @param jwt     JWT of the signed-in user
     * @param request payment fields from the client (userId in the body is replaced with the JWT subject)
     * @return created payment details
     */
    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDto initiatePayment(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InitiatePaymentRequest request) {
        UUID subject = userId(jwt);
        InitiatePaymentRequest payload = new InitiatePaymentRequest(
                request.accountId(),
                subject,
                request.amount(),
                request.currency(),
                request.paymentMethod()
        );
        return dashboardService.initiatePayment(payload);
    }

    /**
     * Reads the user ID from the JWT subject claim.
     *
     * @param jwt authenticated JWT
     * @return user UUID
     */
    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
