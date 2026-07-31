package com.cards.common.error;

/**
 * Business exception for failures when calling another service or external system.
 * Carries a catalog error code and may include a detail message or root cause.
 */
public final class DownstreamException extends BusinessException {

    /**
     * Creates a downstream failure with only a catalog error code.
     *
     * @param errorCode catalog key such as {@code BFF_005} or {@code GW_001}
     */
    public DownstreamException(String errorCode) {
        super(errorCode);
    }

    /**
     * Creates a downstream failure with a catalog code and a more specific detail message.
     *
     * @param errorCode catalog key
     * @param detail    detail text shown to the client when present
     */
    public DownstreamException(String errorCode, String detail) {
        super(errorCode, detail);
    }

    /**
     * Creates a downstream failure with a catalog code and a wrapped cause.
     *
     * @param errorCode catalog key
     * @param cause     underlying exception
     */
    public DownstreamException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
