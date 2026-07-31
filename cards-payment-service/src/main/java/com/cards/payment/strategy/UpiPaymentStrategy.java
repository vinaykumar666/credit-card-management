package com.cards.payment.strategy;

import com.cards.payment.domain.Payment;
import com.cards.payment.domain.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Strategy for {@link PaymentMethod#UPI} payments.
 * Simulates a successful UPI transfer and returns a generated UPI reference.
 */
@Component
public class UpiPaymentStrategy implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(UpiPaymentStrategy.class);

    /**
     * {@inheritDoc}
     *
     * @return {@link PaymentMethod#UPI}
     */
    @Override
    public PaymentMethod supports() {
        return PaymentMethod.UPI;
    }

    /**
     * Processes a UPI payment and returns a success result with a generated reference.
     *
     * @param payment the payment to process
     * @return a successful {@link PaymentResult} with a {@code UPI-} reference
     */
    @Override
    public PaymentResult execute(Payment payment) {
        log.info("Processing UPI payment id={}", payment.getId());
        String ref = "UPI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PaymentResult.success(ref);
    }
}
