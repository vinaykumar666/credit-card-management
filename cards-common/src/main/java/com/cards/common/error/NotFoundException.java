package com.cards.common.error;

/**
 * Business exception when a requested resource does not exist.
 * Maps to catalog codes such as {@code ACCT_001} or {@code PAY_001}.
 */
public final class NotFoundException extends BusinessException {

    /**
     * Creates a not-found failure with a catalog error code.
     *
     * @param errorCode catalog key for the missing resource
     */
    public NotFoundException(String errorCode) {
        super(errorCode);
    }
}
