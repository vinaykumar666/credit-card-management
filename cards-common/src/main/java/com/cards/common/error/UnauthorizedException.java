package com.cards.common.error;

/**
 * Business exception for authentication or authorization failures.
 * Maps to catalog codes such as {@code AUTH_001} or {@code AUTH_004}.
 */
public final class UnauthorizedException extends BusinessException {

    /**
     * Creates an unauthorized failure with a catalog error code.
     *
     * @param errorCode catalog key for the unauthorized case
     */
    public UnauthorizedException(String errorCode) {
        super(errorCode);
    }
}
