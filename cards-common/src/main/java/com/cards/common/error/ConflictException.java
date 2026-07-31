package com.cards.common.error;

/**
 * Business exception for resource conflicts (for example duplicate email or account number).
 * Maps to catalog codes such as {@code AUTH_002} or {@code ACCT_003}.
 */
public final class ConflictException extends BusinessException {

    /**
     * Creates a conflict failure with a catalog error code.
     *
     * @param errorCode catalog key for the conflict case
     */
    public ConflictException(String errorCode) {
        super(errorCode);
    }
}
