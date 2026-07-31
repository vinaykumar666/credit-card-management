package com.cards.payment.strategy;

import com.cards.payment.domain.Payment;
import com.cards.payment.domain.PaymentMethod;

/**
 * Contract for processing a payment with a specific {@link PaymentMethod}.
 * Implementations run the method-specific flow and return success or failure.
 */
public interface PaymentStrategy {

    /**
     * Payment method this strategy handles.
     *
     * @return the supported {@link PaymentMethod}
     */
    PaymentMethod supports();

    /**
     * Runs payment processing for the given payment record.
     *
     * @param payment the payment to process
     * @return result with success flag and either an external reference or a failure reason
     */
    PaymentResult execute(Payment payment);
}
