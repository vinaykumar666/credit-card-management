package com.cards.common.error;

import com.cards.common.channel.ChannelClientContext;
import com.cards.common.channel.ChannelClientHolder;
import com.cards.common.correlation.CorrelationConstants;
import org.slf4j.MDC;

import java.time.Instant;

/**
 * Builds immutable {@link ErrorResponse} bodies from the shared error-code catalog.
 * Also attaches correlation, channel, and client ids from MDC or {@link ChannelClientHolder}.
 */
public final class ErrorResponseFactory {

    private ErrorResponseFactory() {
    }

    /**
     * Creates an error response for the given catalog code and request path.
     * Looks up HTTP status and default message from {@link ErrorCodeProperties}.
     *
     * @param properties error-code catalog
     * @param errorCode  catalog key (for example {@code AUTH_001})
     * @param path       request path shown to the client
     * @return a fully populated {@link ErrorResponse}
     */
    public static ErrorResponse from(ErrorCodeProperties properties, String errorCode, String path) {
        ErrorCodeDefinition definition = properties.require(errorCode);
        ChannelClientContext ctx = ChannelClientHolder.get();
        String correlationId = MDC.get(CorrelationConstants.MDC_CORRELATION_ID);
        String channelId = ctx != null ? ctx.channelId() : MDC.get(CorrelationConstants.MDC_CHANNEL_ID);
        String clientId = ctx != null ? ctx.clientId() : MDC.get(CorrelationConstants.MDC_CLIENT_ID);

        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(definition.httpStatus())
                .errorCode(errorCode)
                .message(definition.message())
                .path(path)
                .correlationId(correlationId)
                .channelId(channelId)
                .clientId(clientId)
                .build();
    }

    /**
     * Creates an error response from a {@link BusinessException}.
     * For validation and downstream failures, a more specific detail message may replace the catalog default.
     *
     * @param properties error-code catalog
     * @param ex         business exception carrying an error code (and optional detail)
     * @param path       request path shown to the client
     * @return a fully populated {@link ErrorResponse}
     */
    public static ErrorResponse from(ErrorCodeProperties properties, BusinessException ex, String path) {
        ErrorResponse base = from(properties, ex.getErrorCode(), path);
        // Why pattern matching switch (Java 21): exhaustive over sealed permits, detail override when present
        String detail = switch (ex) {
            case ValidationBusinessException v when v.getMessage() != null
                    && !v.getMessage().equals(v.getErrorCode()) -> v.getMessage();
            case DownstreamException d when d.getMessage() != null
                    && !d.getMessage().equals(d.getErrorCode()) -> d.getMessage();
            case NotFoundException ignored -> base.message();
            case ConflictException ignored -> base.message();
            case UnauthorizedException ignored -> base.message();
            case BusinessException ignored -> base.message();
        };
        if (detail != null && !detail.equals(base.message()) && !detail.equals(ex.getErrorCode())) {
            return ErrorResponse.builder()
                    .timestamp(base.timestamp())
                    .status(base.status())
                    .errorCode(base.errorCode())
                    .message(detail)
                    .path(base.path())
                    .correlationId(base.correlationId())
                    .channelId(base.channelId())
                    .clientId(base.clientId())
                    .build();
        }
        return base;
    }
}
