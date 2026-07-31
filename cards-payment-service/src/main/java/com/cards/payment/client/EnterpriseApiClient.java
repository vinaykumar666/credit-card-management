package com.cards.payment.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * HTTP client for the enterprise payment authorize API.
 * Uses a circuit breaker and returns a declined response when the API is unavailable.
 */
@Component
public class EnterpriseApiClient {

    private static final Logger log = LoggerFactory.getLogger(EnterpriseApiClient.class);

    private final WebClient enterpriseWebClient;

    /**
     * Creates the client with the enterprise {@link WebClient}.
     *
     * @param enterpriseWebClient WebClient configured with the enterprise API base URL
     */
    public EnterpriseApiClient(WebClient enterpriseWebClient) {
        this.enterpriseWebClient = enterpriseWebClient;
    }

    /**
     * Calls the enterprise authorize endpoint for the given payment request.
     * Protected by the {@code enterpriseApi} circuit breaker.
     *
     * @param request payment details to authorize
     * @return approval result from the enterprise API, or a declined fallback response
     */
    @CircuitBreaker(name = "enterpriseApi", fallbackMethod = "processPaymentFallback")
    public EnterprisePaymentResponse processPayment(EnterprisePaymentRequest request) {
        log.info("Calling enterprise API for payment id={}", request.getPaymentId());
        return enterpriseWebClient.post()
                .uri("/api/v1/enterprise/payments/authorize")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(EnterprisePaymentResponse.class)
                .block();
    }

    /**
     * Fallback when the circuit is open or the enterprise call fails.
     * Returns a declined response with an error message instead of throwing.
     *
     * @param request the original authorize request
     * @param cause   the failure that triggered the fallback
     * @return a declined {@link EnterprisePaymentResponse}
     */
    @SuppressWarnings("unused")
    private EnterprisePaymentResponse processPaymentFallback(EnterprisePaymentRequest request, Throwable cause) {
        log.warn("Enterprise API circuit open/fallback for payment id={}: {}",
                request.getPaymentId(), cause.getMessage());
        String reason = cause instanceof WebClientResponseException ex
                ? "Enterprise API error: " + ex.getStatusCode().value()
                : "Enterprise API unavailable: " + cause.getMessage();
        return EnterprisePaymentResponse.builder()
                .paymentId(request.getPaymentId())
                .approved(false)
                .message(reason)
                .build();
    }
}
