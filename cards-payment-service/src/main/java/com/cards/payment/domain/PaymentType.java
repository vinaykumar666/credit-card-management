package com.cards.payment.domain;

/**
 * Banking purpose of the money movement.
 */
public enum PaymentType {
    /** Pay toward own card / self settlement. */
    CARD_PAYMENT,
    /** Transfer funds to a saved beneficiary. */
    TRANSFER,
    /** Pay a bill / merchant / utility. */
    BILL_PAYMENT
}
