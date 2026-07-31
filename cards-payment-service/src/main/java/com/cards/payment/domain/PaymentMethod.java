package com.cards.payment.domain;

/**
 * Supported ways a customer can pay.
 * Used to pick the matching {@link com.cards.payment.strategy.PaymentStrategy}.
 */
public enum PaymentMethod {
    /** Pay with a credit or debit card. */
    CARD,
    /** Pay through Unified Payments Interface (UPI). */
    UPI,
    /** Pay through internet banking. */
    NET_BANKING,
    /** Pay through an external enterprise payment network. */
    EXTERNAL
}
