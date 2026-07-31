package com.cards.payment.strategy;

import com.cards.payment.client.EnterpriseApiClient;
import com.cards.payment.client.EnterprisePaymentRequest;
import com.cards.payment.client.EnterprisePaymentResponse;
import com.cards.payment.domain.Payment;
import com.cards.payment.domain.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Strategy for {@link PaymentMethod#EXTERNAL} payments.
 * Calls the enterprise payment API and maps approval or decline into a {@link PaymentResult}.
 */
@Component
public class ExternalNetworkPaymentStrategy implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(ExternalNetworkPaymentStrategy.class);

    private final EnterpriseApiClient enterpriseApiClient;

    /**
     * Creates the strategy with the enterprise API client.
     *
     * @param enterpriseApiClient client used to authorize external payments
     */
    public ExternalNetworkPaymentStrategy(EnterpriseApiClient enterpriseApiClient) {
        this.enterpriseApiClient = enterpriseApiClient;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link PaymentMethod#EXTERNAL}
     */
    @Override
    public PaymentMethod supports() {
        return PaymentMethod.EXTERNAL;
    }

    /**
     * Sends the payment to the enterprise API and returns success or failure from the response.
     *
     * @param payment the payment to process
     * @return success with the external reference when approved; otherwise failure with a reason
     */
    @Override
    public PaymentResult execute(Payment payment) {
        log.info("Processing EXTERNAL payment id={} via enterprise API", payment.getId());
        EnterprisePaymentResponse response = enterpriseApiClient.processPayment(
                EnterprisePaymentRequest.builder()
                        .paymentId(payment.getId().toString())
                        .cardToken("tok_" + payment.getAccountId())
                        .amount(payment.getAmount())
                        .currency(payment.getCurrency())
                        .merchantId("cards-platform")
                        .build()
        );

        if (response != null && response.isApproved()) {
            return PaymentResult.success(response.getExternalReference());
        }

        String reason = response != null && response.getMessage() != null
                ? response.getMessage()
                : "External network payment failed";
        return PaymentResult.failure(reason);
    }
}
