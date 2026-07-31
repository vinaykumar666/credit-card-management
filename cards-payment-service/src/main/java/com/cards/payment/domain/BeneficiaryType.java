package com.cards.payment.domain;

/**
 * Kind of beneficiary / payee used in banking transfers.
 */
public enum BeneficiaryType {
    /** Person-to-person or other bank account. */
    PERSON,
    /** Merchant or bill payee (utilities, telecom, etc.). */
    MERCHANT,
    /** Internal same-bank account. */
    INTERNAL
}
