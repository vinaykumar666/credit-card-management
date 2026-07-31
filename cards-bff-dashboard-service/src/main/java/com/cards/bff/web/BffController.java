package com.cards.bff.web;

import com.cards.bff.service.DashboardService;
import com.cards.bff.web.dto.AccountDto;
import com.cards.bff.web.dto.BeneficiaryDto;
import com.cards.bff.web.dto.BeneficiaryRequestDto;
import com.cards.bff.web.dto.DashboardResponse;
import com.cards.bff.web.dto.InitiatePaymentRequest;
import com.cards.bff.web.dto.MakePaymentRequestDto;
import com.cards.bff.web.dto.NotificationDto;
import com.cards.bff.web.dto.PaymentDto;
import com.cards.bff.web.dto.TransactionHistoryDto;
import com.cards.bff.web.dto.TransferMoneyRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * BFF APIs for dashboard, beneficiaries, transfers, and bill payments.
 */
@RestController
@RequestMapping("/bff/v1")
@RequiredArgsConstructor
public class BffController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@AuthenticationPrincipal Jwt jwt) {
        return dashboardService.getDashboard(userId(jwt), jwt.getClaimAsString("email"));
    }

    @GetMapping("/accounts")
    public List<AccountDto> accounts(@AuthenticationPrincipal Jwt jwt) {
        return dashboardService.getAccounts(userId(jwt));
    }

    @GetMapping("/accounts/{id}/transactions")
    public TransactionHistoryDto transactions(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return dashboardService.getTransactions(id, page, size);
    }

    @GetMapping("/notifications")
    public List<NotificationDto> notifications(@AuthenticationPrincipal Jwt jwt) {
        return dashboardService.getNotifications(userId(jwt));
    }

    @GetMapping("/beneficiaries")
    public List<BeneficiaryDto> beneficiaries(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return dashboardService.beneficiaries(userId(jwt), activeOnly);
    }

    @PostMapping("/beneficiaries")
    @ResponseStatus(HttpStatus.CREATED)
    public BeneficiaryDto createBeneficiary(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BeneficiaryRequestDto request) {
        return dashboardService.createBeneficiary(userId(jwt), request);
    }

    @DeleteMapping("/beneficiaries/{id}")
    public BeneficiaryDto deactivateBeneficiary(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        return dashboardService.deactivateBeneficiary(id, userId(jwt));
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDto initiatePayment(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InitiatePaymentRequest request) {
        UUID subject = userId(jwt);
        return dashboardService.initiatePayment(new InitiatePaymentRequest(
                request.accountId(),
                subject,
                request.amount(),
                request.currency(),
                request.paymentMethod()
        ));
    }

    @PostMapping("/payments/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDto transfer(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TransferMoneyRequestDto request) {
        return dashboardService.transfer(userId(jwt), request);
    }

    @PostMapping("/payments/bill-pay")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDto billPay(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MakePaymentRequestDto request) {
        return dashboardService.makePayment(userId(jwt), request);
    }

    @GetMapping("/payments")
    public List<PaymentDto> paymentHistory(@AuthenticationPrincipal Jwt jwt) {
        return dashboardService.paymentHistory(userId(jwt));
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
