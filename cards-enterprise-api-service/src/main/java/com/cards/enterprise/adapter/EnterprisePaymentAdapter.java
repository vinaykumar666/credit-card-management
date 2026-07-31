package com.cards.enterprise.adapter;

import com.cards.enterprise.dto.ExternalPaymentRequest;
import com.cards.enterprise.dto.ExternalPaymentResponse;
import com.cards.enterprise.dto.PaymentAuthorizeRequest;
import com.cards.enterprise.dto.PaymentAuthorizeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Adapter that translates platform payment contracts to and from the proprietary external network API.
 * Keeps the rest of the platform decoupled from external field names and result codes.
 */
@Service
@RequiredArgsConstructor
public class EnterprisePaymentAdapter {

    private final ExternalNetworkClient externalNetworkClient;

    /**
     * Authorizes a platform payment by mapping to the external format, calling the network, then mapping back.
     *
     * @param request platform authorization request
     * @return platform authorization response with approval flag and external reference
     */
    public PaymentAuthorizeResponse authorize(PaymentAuthorizeRequest request) {
        ExternalPaymentRequest externalRequest = toExternal(request);
        ExternalPaymentResponse externalResponse = externalNetworkClient.authorizePayment(externalRequest);
        return toDomain(request.getPaymentId(), externalResponse);
    }

    /**
     * Maps a platform request to the proprietary external request shape.
     *
     * @param request platform authorization request
     * @return external network request
     */
    private ExternalPaymentRequest toExternal(PaymentAuthorizeRequest request) {
        return ExternalPaymentRequest.builder()
                .txnId(request.getPaymentId())
                .cardPanToken(request.getCardToken())
                .amt(request.getAmount())
                .ccy(request.getCurrency())
                .merchantCode(request.getMerchantId())
                .build();
    }

    /**
     * Maps an external network response to the platform response shape.
     * Treats result code {@code 00} as approved.
     *
     * @param paymentId platform payment id to echo in the response
     * @param external  external network response
     * @return platform authorization response
     */
    private PaymentAuthorizeResponse toDomain(String paymentId, ExternalPaymentResponse external) {
        boolean approved = "00".equals(external.getResultCode());
        return PaymentAuthorizeResponse.builder()
                .paymentId(paymentId)
                .approved(approved)
                .externalReference(external.getExternalRef())
                .message(external.getResultMessage())
                .build();
    }
}
