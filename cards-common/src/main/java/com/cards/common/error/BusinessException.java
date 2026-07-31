package com.cards.common.error;

/**
 * Base type for expected business failures across card services.
 * Sealed so handlers can pattern-match over the known subtypes and always carry a catalog {@code errorCode}.
 */
public sealed class BusinessException extends RuntimeException
        permits NotFoundException, ConflictException, UnauthorizedException, ValidationBusinessException, DownstreamException {

    private final String errorCode;

    /**
     * Creates a business failure whose exception message is the error code itself.
     *
     * @param errorCode catalog key (for example {@code AUTH_001})
     */
    public BusinessException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    /**
     * Creates a business failure with a catalog code and an optional detail message.
     * If {@code detail} is null, the message falls back to the error code.
     *
     * @param errorCode catalog key
     * @param detail    human-readable detail, or null to use the code as the message
     */
    public BusinessException(String errorCode, String detail) {
        super(detail == null ? errorCode : detail);
        this.errorCode = errorCode;
    }

    /**
     * Creates a business failure with a catalog code and a wrapped cause.
     *
     * @param errorCode catalog key
     * @param cause     underlying exception
     */
    public BusinessException(String errorCode, Throwable cause) {
        super(errorCode, cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the catalog error code associated with this failure.
     *
     * @return error code string such as {@code AUTH_001}
     */
    public String getErrorCode() {
        return errorCode;
    }
}
