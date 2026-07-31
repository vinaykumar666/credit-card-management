package com.cards.enterprise.adapter;

import com.cards.enterprise.dto.ExternalPaymentRequest;
import com.cards.enterprise.dto.ExternalPaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates a proprietary external / core-banking payment API.
 * Not a real HTTP client — intentionally isolates vendor quirks behind this boundary
 * and can randomly approve or decline based on configuration.
 */
@Slf4j
@Component
public class ExternalNetworkClient {

    private final boolean simulateFailures;
    private final double successRate;

    /**
     * Creates the simulated client with failure-behavior settings.
     *
     * @param simulateFailures when {@code true}, randomly declines some authorizations
     * @param successRate      probability of approval when failures are simulated (0.0–1.0)
     */
    public ExternalNetworkClient(
            @Value("${app.external-network.simulate-failures:true}") boolean simulateFailures,
            @Value("${app.external-network.success-rate:0.90}") double successRate) {
        this.simulateFailures = simulateFailures;
        this.successRate = successRate;
    }

    /**
     * Simulates authorizing a payment on the external network.
     * Returns result code {@code 00} on approval or {@code 05} on decline.
     *
     * @param request proprietary external payment request
     * @return simulated network response with result code and external reference
     */
    public ExternalPaymentResponse authorizePayment(ExternalPaymentRequest request) {
        log.info("Calling external network for txnId={}", request.getTxnId());

        boolean success = !simulateFailures || ThreadLocalRandom.current().nextDouble() < successRate;
        String externalRef = "EXT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        if (success) {
            return ExternalPaymentResponse.builder()
                    .resultCode("00")
                    .externalRef(externalRef)
                    .resultMessage("APPROVED")
                    .build();
        }

        return ExternalPaymentResponse.builder()
                .resultCode("05")
                .externalRef(externalRef)
                .resultMessage("DECLINED_BY_ISSUER")
                .build();
    }
}
