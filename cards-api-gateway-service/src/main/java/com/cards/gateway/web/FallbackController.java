package com.cards.gateway.web;

import com.cards.common.correlation.CorrelationConstants;
import com.cards.common.error.ErrorCodeProperties;
import com.cards.common.error.ErrorCodes;
import com.cards.common.error.ErrorResponse;
import com.cards.common.error.ErrorResponseFactory;
import com.cards.gateway.filter.CorrelationIdGatewayFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Circuit-breaker / route fallback endpoints under {@code /fallback}.
 * Returns a standard gateway error ({@code GW_001}) when a downstream route is unavailable.
 */
@RestController
@RequestMapping("/fallback")
@RequiredArgsConstructor
public class FallbackController {

    private final ErrorCodeProperties errorCodeProperties;

    /**
     * Fallback for the authentication service route.
     *
     * @param exchange current server exchange
     * @return gateway unavailable error response
     */
    @RequestMapping("/auth")
    public Mono<ResponseEntity<ErrorResponse>> authFallback(ServerWebExchange exchange) {
        return fallback(exchange);
    }

    /**
     * Fallback for the accounts service route.
     *
     * @param exchange current server exchange
     * @return gateway unavailable error response
     */
    @RequestMapping("/accounts")
    public Mono<ResponseEntity<ErrorResponse>> accountsFallback(ServerWebExchange exchange) {
        return fallback(exchange);
    }

    /**
     * Fallback for the payments service route.
     *
     * @param exchange current server exchange
     * @return gateway unavailable error response
     */
    @RequestMapping("/payments")
    public Mono<ResponseEntity<ErrorResponse>> paymentsFallback(ServerWebExchange exchange) {
        return fallback(exchange);
    }

    /**
     * Fallback for the notifications service route.
     *
     * @param exchange current server exchange
     * @return gateway unavailable error response
     */
    @RequestMapping("/notifications")
    public Mono<ResponseEntity<ErrorResponse>> notificationsFallback(ServerWebExchange exchange) {
        return fallback(exchange);
    }

    /**
     * Fallback for the enterprise service route.
     *
     * @param exchange current server exchange
     * @return gateway unavailable error response
     */
    @RequestMapping("/enterprise")
    public Mono<ResponseEntity<ErrorResponse>> enterpriseFallback(ServerWebExchange exchange) {
        return fallback(exchange);
    }

    /**
     * Fallback for the BFF dashboard service route.
     *
     * @param exchange current server exchange
     * @return gateway unavailable error response
     */
    @RequestMapping("/bff")
    public Mono<ResponseEntity<ErrorResponse>> bffFallback(ServerWebExchange exchange) {
        return fallback(exchange);
    }

    /**
     * Builds a {@code GW_001} error response using correlation and tenant IDs from the exchange when present.
     *
     * @param exchange current server exchange
     * @return mono of the error response entity
     */
    private Mono<ResponseEntity<ErrorResponse>> fallback(ServerWebExchange exchange) {
        String correlationId = exchange.getAttribute(CorrelationIdGatewayFilter.CORRELATION_ID_ATTR);
        if (correlationId == null) {
            correlationId = exchange.getRequest().getHeaders().getFirst(CorrelationConstants.CORRELATION_ID_HEADER);
        }
        String channelId = exchange.getAttribute(CorrelationIdGatewayFilter.CHANNEL_ID_ATTR);
        if (channelId == null) {
            channelId = exchange.getRequest().getHeaders().getFirst(CorrelationConstants.CHANNEL_ID_HEADER);
        }
        String clientId = exchange.getAttribute(CorrelationIdGatewayFilter.CLIENT_ID_ATTR);
        if (clientId == null) {
            clientId = exchange.getRequest().getHeaders().getFirst(CorrelationConstants.CLIENT_ID_HEADER);
        }

        if (correlationId != null) {
            MDC.put(CorrelationConstants.MDC_CORRELATION_ID, correlationId);
        }
        if (channelId != null) {
            MDC.put(CorrelationConstants.MDC_CHANNEL_ID, channelId);
        }
        if (clientId != null) {
            MDC.put(CorrelationConstants.MDC_CLIENT_ID, clientId);
        }
        try {
            ErrorResponse body = ErrorResponseFactory.from(
                    errorCodeProperties,
                    ErrorCodes.GW_001,
                    exchange.getRequest().getURI().getPath()
            );
            return Mono.just(ResponseEntity.status(body.status()).body(body));
        } finally {
            MDC.remove(CorrelationConstants.MDC_CORRELATION_ID);
            MDC.remove(CorrelationConstants.MDC_CHANNEL_ID);
            MDC.remove(CorrelationConstants.MDC_CLIENT_ID);
        }
    }
}
