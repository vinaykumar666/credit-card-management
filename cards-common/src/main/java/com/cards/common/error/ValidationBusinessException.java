package com.cards.common.error;

/**
 * Business exception for domain or request validation failures that are not bean-validation errors.
 * Carries a catalog error code and an optional detail message for the client.
 */
public final class ValidationBusinessException extends BusinessException {

    /**
     * Creates a validation failure with only a catalog error code.
     *
     * @param errorCode catalog key such as {@code COMMON_001} or {@code ACCT_002}
     */
    public ValidationBusinessException(String errorCode) {
        super(errorCode);
    }

    /**
     * Creates a validation failure with a catalog code and a specific detail message.
     *
     * @param errorCode catalog key
     * @param detail    detail text shown to the client when present
     */
    public ValidationBusinessException(String errorCode, String detail) {
        super(errorCode, detail);
    }
}
