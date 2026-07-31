package com.cards.gateway.filter;

import com.cards.common.correlation.CorrelationConstants;
import com.cards.common.error.ErrorCodeProperties;
import com.cards.common.error.ErrorCodes;
import com.cards.common.error.ErrorResponse;
import com.cards.common.error.ErrorResponseFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Highest-precedence global filter that sets correlation and tenant headers on every request.
 * Creates a correlation ID when missing; optionally requires channel and client headers except on exempt paths.
 */
@Component
public class CorrelationIdGatewayFilter implements GlobalFilter, Ordered {

    /** Exchange attribute key for the resolved correlation ID. */
    public static final String CORRELATION_ID_ATTR = "correlationId";
    /** Exchange attribute key for the channel ID. */
    public static final String CHANNEL_ID_ATTR = "channelId";
    /** Exchange attribute key for the client ID. */
    public static final String CLIENT_ID_ATTR = "clientId";

    private final ObjectMapper objectMapper;
    private final ErrorCodeProperties errorCodeProperties;
    private final boolean requireTenantHeaders;

    /**
     * Creates the filter with JSON error support and optional tenant-header enforcement.
     *
     * @param objectMapper          used to serialize error bodies
     * @param errorCodeProperties   error code catalog for responses
     * @param requireTenantHeaders  when {@code true}, non-exempt requests must send channel and client headers
     */
    public CorrelationIdGatewayFilter(
            ObjectMapper objectMapper,
            ErrorCodeProperties errorCodeProperties,
            @Value("${app.gateway.require-tenant-headers:true}") boolean requireTenantHeaders
    ) {
        this.objectMapper = objectMapper;
        this.errorCodeProperties = errorCodeProperties;
        this.requireTenantHeaders = requireTenantHeaders;
    }

    /**
     * Propagates or generates correlation/tenant headers, stores them on the exchange and in MDC, then continues.
     *
     * @param exchange current server exchange
     * @param chain    remaining gateway filters
     * @return completion signal, or a bad-request response when required tenant headers are missing
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CorrelationConstants.CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String channelId = blankToEmpty(exchange.getRequest().getHeaders().getFirst(CorrelationConstants.CHANNEL_ID_HEADER));
        String clientId = blankToEmpty(exchange.getRequest().getHeaders().getFirst(CorrelationConstants.CLIENT_ID_HEADER));

        String path = exchange.getRequest().getURI().getPath();
        if (requireTenantHeaders && !isExemptFromTenantHeaders(path)
                && (channelId.isBlank() || clientId.isBlank())) {
            return missingTenantHeaders(exchange, correlationId);
        }

        String finalCorrelationId = correlationId;
        exchange.getAttributes().put(CORRELATION_ID_ATTR, finalCorrelationId);
        exchange.getAttributes().put(CHANNEL_ID_ATTR, channelId);
        exchange.getAttributes().put(CLIENT_ID_ATTR, clientId);

        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate()
                .header(CorrelationConstants.CORRELATION_ID_HEADER, finalCorrelationId);
        if (!channelId.isBlank()) {
            requestBuilder.header(CorrelationConstants.CHANNEL_ID_HEADER, channelId);
        }
        if (!clientId.isBlank()) {
            requestBuilder.header(CorrelationConstants.CLIENT_ID_HEADER, clientId);
        }

        ServerWebExchange mutatedExchange = exchange.mutate().request(requestBuilder.build()).build();
        mutatedExchange.getResponse().getHeaders().set(CorrelationConstants.CORRELATION_ID_HEADER, finalCorrelationId);
        if (!channelId.isBlank()) {
            mutatedExchange.getResponse().getHeaders().set(CorrelationConstants.CHANNEL_ID_HEADER, channelId);
        }
        if (!clientId.isBlank()) {
            mutatedExchange.getResponse().getHeaders().set(CorrelationConstants.CLIENT_ID_HEADER, clientId);
        }

        MDC.put(CorrelationConstants.MDC_CORRELATION_ID, finalCorrelationId);
        MDC.put(CorrelationConstants.MDC_CHANNEL_ID, channelId);
        MDC.put(CorrelationConstants.MDC_CLIENT_ID, clientId);
        return chain.filter(mutatedExchange)
                .doFinally(signal -> {
                    MDC.remove(CorrelationConstants.MDC_CORRELATION_ID);
                    MDC.remove(CorrelationConstants.MDC_CHANNEL_ID);
                    MDC.remove(CorrelationConstants.MDC_CLIENT_ID);
                });
    }

    /**
     * Paths that do not require channel/client headers (auth, actuator, fallback).
     *
     * @param path request path
     * @return {@code true} when tenant headers are not required
     */
    private boolean isExemptFromTenantHeaders(String path) {
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/actuator")
                || path.startsWith("/fallback");
    }

    /**
     * Returns HTTP 400 with a structured error when required tenant headers are missing.
     *
     * @param exchange      current exchange
     * @param correlationId correlation ID to include on the response
     * @return write completion for the error body
     */
    private Mono<Void> missingTenantHeaders(ServerWebExchange exchange, String correlationId) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(CorrelationConstants.CORRELATION_ID_HEADER, correlationId);

        MDC.put(CorrelationConstants.MDC_CORRELATION_ID, correlationId);
        ErrorResponse body = ErrorResponseFactory.from(
                errorCodeProperties,
                ErrorCodes.GW_003,
                exchange.getRequest().getURI().getPath()
        );
        MDC.remove(CorrelationConstants.MDC_CORRELATION_ID);

        try {
            DataBuffer buffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(body));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }

    /**
     * Trims a header value, or returns an empty string when {@code null}.
     *
     * @param value raw header value
     * @return trimmed value or empty string
     */
    private static String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Runs before other global filters so correlation/tenant context is available early.
     *
     * @return highest precedence order value
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
