package com.cards.common.eventstore;

import com.cards.common.correlation.CorrelationConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Records one {@code HTTP_FOOTFALL} row per inbound HTTP request (entry into the service).
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class AppEventFootfallFilter extends OncePerRequestFilter {

    private final AppEventStore appEventStore;
    private final String serviceName;

    public AppEventFootfallFilter(AppEventStore appEventStore, String serviceName) {
        this.appEventStore = appEventStore;
        this.serviceName = serviceName;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long started = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            UUID userId = parseUuid(request.getHeader("X-User-Id"));
            appEventStore.record(new AppEventRecord(
                    AppEventNames.HTTP_FOOTFALL,
                    "FOOTFALL",
                    serviceName,
                    null,
                    userId,
                    request.getRemoteUser(),
                    null,
                    null,
                    blankToNull(request.getHeader(CorrelationConstants.CORRELATION_ID_HEADER)),
                    blankToNull(request.getHeader(CorrelationConstants.CHANNEL_ID_HEADER)),
                    blankToNull(request.getHeader(CorrelationConstants.CLIENT_ID_HEADER)),
                    request.getMethod(),
                    request.getRequestURI(),
                    String.valueOf(response.getStatus()),
                    System.currentTimeMillis() - started,
                    null
            ));
        }
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
