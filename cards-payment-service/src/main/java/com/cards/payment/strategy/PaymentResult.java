package com.cards.payment.strategy;

import lombok.Builder;
import lombok.Value;

/**
 * Outcome of running a {@link PaymentStrategy}.
 * Either success with an external reference, or failure with a reason.
 */
@Value
@Builder
public class PaymentResult {

    /** {@code true} when the strategy succeeded. */
    boolean success;
    /** Reference from the processor on success; may be null on failure. */
    String externalRef;
    /** Human-readable reason on failure; may be null on success. */
    String failureReason;

    /**
     * Builds a successful result with an external reference.
     *
     * @param externalRef reference from the payment processor
     * @return a success {@link PaymentResult}
     */
    public static PaymentResult success(String externalRef) {
        return PaymentResult.builder()
                .success(true)
                .externalRef(externalRef)
                .build();
    }

    /**
     * Builds a failed result with a reason.
     *
     * @param failureReason why the payment failed
     * @return a failure {@link PaymentResult}
     */
    public static PaymentResult failure(String failureReason) {
        return PaymentResult.builder()
                .success(false)
                .failureReason(failureReason)
                .build();
    }
}
