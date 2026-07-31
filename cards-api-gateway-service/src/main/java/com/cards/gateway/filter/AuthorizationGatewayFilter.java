package com.cards.gateway.filter;

import com.cards.common.api.ApiError;
import com.cards.common.correlation.CorrelationConstants;
import com.cards.gateway.security.TokenValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Optional global filter that checks for a valid Authorization Bearer header on protected routes.
 * Disabled by default ({@code app.gateway.require-auth-header=false}); full JWT validation stays in the auth service and Spring Security.
 */
@Component
public class AuthorizationGatewayFilter implements GlobalFilter, Ordered {

    private final TokenValidator tokenValidator;
    private final ObjectMapper objectMapper;
    private final boolean requireAuthHeader;

    /**
     * Creates the filter with a token validator and optional auth-header enforcement.
     *
     * @param tokenValidator     checks whether the Authorization header is acceptable
     * @param objectMapper       used to serialize unauthorized error bodies
     * @param requireAuthHeader  when {@code true}, non-public paths must pass {@link TokenValidator#isValid(String)}
     */
    public AuthorizationGatewayFilter(
            TokenValidator tokenValidator,
            ObjectMapper objectMapper,
            @Value("${app.gateway.require-auth-header:false}") boolean requireAuthHeader) {
        this.tokenValidator = tokenValidator;
        this.objectMapper = objectMapper;
        this.requireAuthHeader = requireAuthHeader;
    }

    /**
     * Continues the chain when auth is not required or the Bearer token is valid; otherwise returns 401.
     *
     * @param exchange current server exchange
     * @param chain    remaining gateway filters
     * @return completion signal or an unauthorized response
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!requireAuthHeader || isPublicPath(exchange.getRequest().getURI().getPath())) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (tokenValidator.isValid(authorization)) {
            return chain.filter(exchange);
        }

        return unauthorized(exchange);
    }

    /**
     * Paths that skip the optional Bearer presence check.
     *
     * @param path request path
     * @return {@code true} for auth, actuator, fallback, and enterprise health-check paths
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/actuator")
                || path.startsWith("/fallback")
                || path.startsWith("/api/v1/enterprise/health-check");
    }

    /**
     * Writes an HTTP 401 JSON {@link ApiError} when the Authorization header is missing or invalid.
     *
     * @param exchange current exchange
     * @return write completion for the error body
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String correlationId = exchange.getAttribute(CorrelationIdGatewayFilter.CORRELATION_ID_ATTR);
        if (correlationId == null) {
            correlationId = exchange.getRequest().getHeaders().getFirst(CorrelationConstants.CORRELATION_ID_HEADER);
        }

        ApiError body = ApiError.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Missing or invalid Authorization Bearer token")
                .path(exchange.getRequest().getURI().getPath())
                .correlationId(correlationId)
                .build();

        try {
            DataBuffer buffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(body));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }

    /**
     * Runs just after {@link CorrelationIdGatewayFilter} so correlation IDs are available on 401 responses.
     *
     * @return order value one step after highest precedence
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
