package com.cards.common.correlation;

/**
 * Shared HTTP header names and SLF4J MDC keys for correlation and tenant identity.
 * Services use the same names so logs and error responses stay consistent.
 */
public final class CorrelationConstants {

    /** HTTP header for the request correlation id. */
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    /** HTTP header for the channel / partner id. */
    public static final String CHANNEL_ID_HEADER = "X-Channel-Id";
    /** HTTP header for the client application id. */
    public static final String CLIENT_ID_HEADER = "X-Client-Id";
    /** MDC key for the correlation id in logs. */
    public static final String MDC_CORRELATION_ID = "correlationId";
    /** MDC key for the channel id in logs. */
    public static final String MDC_CHANNEL_ID = "channelId";
    /** MDC key for the client id in logs. */
    public static final String MDC_CLIENT_ID = "clientId";

    private CorrelationConstants() {
    }
}
