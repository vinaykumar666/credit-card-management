package com.cards.payment.domain;

/**
 * Lifecycle status of a payment record.
 */
public enum PaymentStatus {
    /** Payment was created and is waiting to be processed. */
    PENDING,
    /** Payment finished successfully. */
    COMPLETED,
    /** Payment did not succeed. */
    FAILED
}
