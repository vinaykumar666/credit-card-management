package com.cards.bff.config;

import com.cards.common.correlation.CorrelationConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that ensures every request has a correlation ID for tracing.
 * Reuses the incoming header when present, otherwise creates a new UUID, and stores it in MDC and the response.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    /**
     * Reads or creates a correlation ID, puts it in MDC and the response, then continues the filter chain.
     *
     * @param request     current HTTP request
     * @param response    current HTTP response
     * @param filterChain remaining filters
     * @throws ServletException if the chain fails with a servlet error
     * @throws IOException      if an I/O error occurs while processing the request
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = headerOrNew(request.getHeader(CorrelationConstants.CORRELATION_ID_HEADER));
        MDC.put(CorrelationConstants.MDC_CORRELATION_ID, correlationId);
        response.setHeader(CorrelationConstants.CORRELATION_ID_HEADER, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CorrelationConstants.MDC_CORRELATION_ID);
        }
    }

    /**
     * Returns a trimmed header value, or a new UUID when the value is missing or blank.
     *
     * @param value raw header value, may be {@code null}
     * @return existing correlation ID or a newly generated one
     */
    private static String headerOrNew(String value) {
        return value == null || value.isBlank() ? UUID.randomUUID().toString() : value.trim();
    }
}
