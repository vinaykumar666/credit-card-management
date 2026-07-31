package com.cards.payment.strategy;

import com.cards.payment.domain.Payment;
import com.cards.payment.domain.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Strategy for {@link PaymentMethod#CARD} payments.
 * Simulates a successful card charge and returns a generated card reference.
 */
@Component
public class CardPaymentStrategy implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(CardPaymentStrategy.class);

    /**
     * {@inheritDoc}
     *
     * @return {@link PaymentMethod#CARD}
     */
    @Override
    public PaymentMethod supports() {
        return PaymentMethod.CARD;
    }

    /**
     * Processes a card payment and returns a success result with a generated reference.
     *
     * @param payment the payment to process
     * @return a successful {@link PaymentResult} with a {@code CARD-} reference
     */
    @Override
    public PaymentResult execute(Payment payment) {
        log.info("Processing CARD payment id={}", payment.getId());
        String ref = "CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PaymentResult.success(ref);
    }
}
