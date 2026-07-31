package com.cards.payment.strategy;

import com.cards.payment.domain.Payment;
import com.cards.payment.domain.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Strategy for {@link PaymentMethod#NET_BANKING} payments.
 * Simulates a successful net-banking transfer and returns a generated reference.
 */
@Component
public class NetBankingPaymentStrategy implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(NetBankingPaymentStrategy.class);

    /**
     * {@inheritDoc}
     *
     * @return {@link PaymentMethod#NET_BANKING}
     */
    @Override
    public PaymentMethod supports() {
        return PaymentMethod.NET_BANKING;
    }

    /**
     * Processes a net-banking payment and returns a success result with a generated reference.
     *
     * @param payment the payment to process
     * @return a successful {@link PaymentResult} with a {@code NB-} reference
     */
    @Override
    public PaymentResult execute(Payment payment) {
        log.info("Processing NET_BANKING payment id={}", payment.getId());
        String ref = "NB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PaymentResult.success(ref);
    }
}
