package com.cards.bff.client;

import com.cards.bff.web.dto.AccountDto;
import com.cards.bff.web.dto.BeneficiaryDto;
import com.cards.bff.web.dto.InitiatePaymentRequest;
import com.cards.bff.web.dto.NotificationDto;
import com.cards.bff.web.dto.PaymentDto;
import com.cards.bff.web.dto.TransactionHistoryDto;
import com.cards.common.channel.ChannelClientContext;
import com.cards.common.channel.ChannelClientHolder;
import com.cards.common.correlation.CorrelationConstants;
import com.cards.common.error.DownstreamException;
import com.cards.common.error.ErrorCodes;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * HTTP client for calling backend APIs through the API gateway.
 * Propagates JWT and tenant/correlation headers and uses a circuit breaker with fallbacks that map failures to {@link DownstreamException}.
 */
@Component
public class DownstreamClient {

    private static final Logger log = LoggerFactory.getLogger(DownstreamClient.class);
    private static final ParameterizedTypeReference<List<AccountDto>> ACCOUNT_LIST =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<NotificationDto>> NOTIFICATION_LIST =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<BeneficiaryDto>> BENEFICIARY_LIST =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<PaymentDto>> PAYMENT_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient gatewayWebClient;

    /**
     * Creates a client that uses the given gateway WebClient.
     *
     * @param gatewayWebClient WebClient configured with the gateway base URL
     */
    public DownstreamClient(WebClient gatewayWebClient) {
        this.gatewayWebClient = gatewayWebClient;
    }

    /**
     * Fetches all accounts for a user from the accounts API via the gateway.
     *
     * @param userId user whose accounts are requested
     * @return list of accounts, or throws via the circuit-breaker fallback on failure
     */
    @CircuitBreaker(name = "downstreamGateway", fallbackMethod = "getAccountsByUserFallback")
    public List<AccountDto> getAccountsByUser(UUID userId) {
        return gatewayWebClient.get()
                .uri("/api/v1/accounts/user/{userId}", userId)
                .headers(propagateHeaders())
                .retrieve()
                .bodyToMono(ACCOUNT_LIST)
                .block();
    }

    /**
     * Fetches a page of transactions for an account from the accounts API via the gateway.
     *
     * @param accountId account to load transactions for
     * @param page      zero-based page index
     * @param size      page size
     * @return paged transaction history
     */
    @CircuitBreaker(name = "downstreamGateway", fallbackMethod = "getTransactionsFallback")
    public TransactionHistoryDto getTransactions(UUID accountId, int page, int size) {
        return gatewayWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/accounts/{id}/transactions")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(accountId))
                .headers(propagateHeaders())
                .retrieve()
                .bodyToMono(TransactionHistoryDto.class)
                .block();
    }

    /**
     * Fetches notifications for a user from the notifications API via the gateway.
     *
     * @param userId user whose notifications are requested
     * @return list of notifications
     */
    @CircuitBreaker(name = "downstreamGateway", fallbackMethod = "getNotificationsByUserFallback")
    public List<NotificationDto> getNotificationsByUser(UUID userId) {
        return gatewayWebClient.get()
                .uri("/api/v1/notifications/user/{userId}", userId)
                .headers(propagateHeaders())
                .retrieve()
                .bodyToMono(NOTIFICATION_LIST)
                .block();
    }

    /**
     * Fetches a single payment by ID from the payments API via the gateway.
     *
     * @param paymentId payment identifier
     * @return payment details
     */
    @CircuitBreaker(name = "downstreamGateway", fallbackMethod = "getPaymentByIdFallback")
    public PaymentDto getPaymentById(UUID paymentId) {
        return gatewayWebClient.get()
                .uri("/api/v1/payments/{id}", paymentId)
                .headers(propagateHeaders())
                .retrieve()
                .bodyToMono(PaymentDto.class)
                .block();
    }

    /**
     * Starts a new payment by posting to the payments API via the gateway.
     *
     * @param request payment initiation payload
     * @return created or accepted payment details
     */
    @CircuitBreaker(name = "downstreamGateway", fallbackMethod = "initiatePaymentFallback")
    public PaymentDto initiatePayment(InitiatePaymentRequest request) {
        return gatewayWebClient.post()
                .uri("/api/v1/payments")
                .headers(propagateHeaders())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentDto.class)
                .block();
    }

    @CircuitBreaker(name = "downstreamGateway", fallbackMethod = "transferFallback")
    public PaymentDto transfer(Object body) {
        return gatewayWebClient.post()
                .uri("/api/v1/payments/transfer")
                .headers(propagateHeaders())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(PaymentDto.class)
                .block();
    }

    @CircuitBreaker(name = "downstreamGateway", fallbackMethod = "billPayFallback")
    public PaymentDto billPay(Object body) {
        return gatewayWebClient.post()
                .uri("/api/v1/payments/bill-pay")
                .headers(propagateHeaders())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(PaymentDto.class)
                .block();
    }

    @CircuitBreaker(name = "downstreamGateway", fallbackMethod = "paymentHistoryFallback")
    public List<PaymentDto> paymentHistory(UUID userId) {
        return gatewayWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/payments").queryParam("userId", userId).build())
                .headers(propagateHeaders())
                .retrieve()
                .bodyToMono(PAYMENT_LIST)
                .block();
    }

    @CircuitBreaker(name = "downstreamGateway", fallbackMethod = "beneficiariesFallback")
    public List<BeneficiaryDto> listBeneficiaries(UUID userId, boolean activeOnly) {
        return gatewayWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/beneficiaries")
                        .queryParam("userId", userId)
                        .queryParam("activeOnly", activeOnly)
                        .build())
                .headers(propagateHeaders())
                .retrieve()
                .bodyToMono(BENEFICIARY_LIST)
                .block();
    }

    @CircuitBreaker(name = "downstreamGateway", fallbackMethod = "createBeneficiaryFallback")
    public BeneficiaryDto createBeneficiary(Object body) {
        return gatewayWebClient.post()
                .uri("/api/v1/beneficiaries")
                .headers(propagateHeaders())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(BeneficiaryDto.class)
                .block();
    }

    @CircuitBreaker(name = "downstreamGateway", fallbackMethod = "deactivateBeneficiaryFallback")
    public BeneficiaryDto deactivateBeneficiary(UUID id, UUID userId) {
        return gatewayWebClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/beneficiaries/{id}")
                        .queryParam("userId", userId)
                        .build(id))
                .headers(propagateHeaders())
                .retrieve()
                .bodyToMono(BeneficiaryDto.class)
                .block();
    }

    /**
     * Circuit-breaker fallback for {@link #getAccountsByUser(UUID)}.
     *
     * @param userId user ID from the original call
     * @param cause  failure cause
     * @return never returns normally
     * @throws DownstreamException always, wrapping the failure
     */
    @SuppressWarnings("unused")
    private List<AccountDto> getAccountsByUserFallback(UUID userId, Throwable cause) {
        throw toDownstream(cause, "accounts for user " + userId);
    }

    /**
     * Circuit-breaker fallback for {@link #getTransactions(UUID, int, int)}.
     *
     * @param accountId account ID from the original call
     * @param page      page from the original call
     * @param size      size from the original call
     * @param cause     failure cause
     * @return never returns normally
     * @throws DownstreamException always, wrapping the failure
     */
    @SuppressWarnings("unused")
    private TransactionHistoryDto getTransactionsFallback(UUID accountId, int page, int size, Throwable cause) {
        throw toDownstream(cause, "transactions for account " + accountId);
    }

    /**
     * Circuit-breaker fallback for {@link #getNotificationsByUser(UUID)}.
     *
     * @param userId user ID from the original call
     * @param cause  failure cause
     * @return never returns normally
     * @throws DownstreamException always, wrapping the failure
     */
    @SuppressWarnings("unused")
    private List<NotificationDto> getNotificationsByUserFallback(UUID userId, Throwable cause) {
        throw toDownstream(cause, "notifications for user " + userId);
    }

    /**
     * Circuit-breaker fallback for {@link #getPaymentById(UUID)}.
     *
     * @param paymentId payment ID from the original call
     * @param cause     failure cause
     * @return never returns normally
     * @throws DownstreamException always, wrapping the failure
     */
    @SuppressWarnings("unused")
    private PaymentDto getPaymentByIdFallback(UUID paymentId, Throwable cause) {
        throw toDownstream(cause, "payment " + paymentId);
    }

    /**
     * Circuit-breaker fallback for {@link #initiatePayment(InitiatePaymentRequest)}.
     *
     * @param request original payment request
     * @param cause   failure cause
     * @return never returns normally
     * @throws DownstreamException always, wrapping the failure
     */
    @SuppressWarnings("unused")
    private PaymentDto initiatePaymentFallback(InitiatePaymentRequest request, Throwable cause) {
        throw toDownstream(cause, "initiate payment");
    }

    @SuppressWarnings("unused")
    private PaymentDto transferFallback(Object body, Throwable cause) {
        throw toDownstream(cause, "transfer money");
    }

    @SuppressWarnings("unused")
    private PaymentDto billPayFallback(Object body, Throwable cause) {
        throw toDownstream(cause, "bill payment");
    }

    @SuppressWarnings("unused")
    private List<PaymentDto> paymentHistoryFallback(UUID userId, Throwable cause) {
        throw toDownstream(cause, "payment history for " + userId);
    }

    @SuppressWarnings("unused")
    private List<BeneficiaryDto> beneficiariesFallback(UUID userId, boolean activeOnly, Throwable cause) {
        throw toDownstream(cause, "beneficiaries for " + userId);
    }

    @SuppressWarnings("unused")
    private BeneficiaryDto createBeneficiaryFallback(Object body, Throwable cause) {
        throw toDownstream(cause, "create beneficiary");
    }

    @SuppressWarnings("unused")
    private BeneficiaryDto deactivateBeneficiaryFallback(UUID id, UUID userId, Throwable cause) {
        throw toDownstream(cause, "deactivate beneficiary " + id);
    }

    /**
     * Maps a failure into a {@link DownstreamException}, reusing one if already present.
     *
     * @param cause   original failure
     * @param context short description of the failed call for logging and error detail
     * @return downstream exception to throw to callers
     */
    private DownstreamException toDownstream(Throwable cause, String context) {
        log.warn("Downstream gateway call failed for {}: {}", context, cause.getMessage());
        if (cause instanceof DownstreamException downstream) {
            return downstream;
        }
        String detail = cause instanceof WebClientResponseException ex
                ? "Downstream error " + ex.getStatusCode().value() + " for " + context
                : "Downstream unavailable for " + context;
        return new DownstreamException(ErrorCodes.BFF_005, detail);
    }

    /**
     * Builds a header consumer that copies JWT Bearer, correlation, channel, and client headers.
     *
     * @return consumer applied to outbound request headers
     */
    private Consumer<HttpHeaders> propagateHeaders() {
        return headers -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                headers.setBearerAuth(jwtAuth.getToken().getTokenValue());
            }

            ChannelClientContext ctx = ChannelClientHolder.get();
            String correlationId = ctx != null && !ctx.correlationId().isBlank()
                    ? ctx.correlationId()
                    : MDC.get(CorrelationConstants.MDC_CORRELATION_ID);
            String channelId = ctx != null ? ctx.channelId() : MDC.get(CorrelationConstants.MDC_CHANNEL_ID);
            String clientId = ctx != null ? ctx.clientId() : MDC.get(CorrelationConstants.MDC_CLIENT_ID);

            if (correlationId != null && !correlationId.isBlank()) {
                headers.set(CorrelationConstants.CORRELATION_ID_HEADER, correlationId);
            }
            if (channelId != null && !channelId.isBlank()) {
                headers.set(CorrelationConstants.CHANNEL_ID_HEADER, channelId);
            }
            if (clientId != null && !clientId.isBlank()) {
                headers.set(CorrelationConstants.CLIENT_ID_HEADER, clientId);
            }
        };
    }

    /**
     * Returns the given list, or an empty list when the value is {@code null}.
     *
     * @param <T>   element type
     * @param value list that may be null (for example from WebClient)
     * @return original list or an empty list
     */
    public static <T> List<T> orEmpty(List<T> value) {
        return value == null ? Collections.emptyList() : value;
    }
}
