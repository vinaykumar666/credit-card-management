package com.cards.auth.config;

import com.cards.common.channel.ChannelClientContext;
import com.cards.common.channel.ChannelClientHolder;
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
 * Servlet filter that sets correlation, channel, and client IDs on every request.
 * Reads IDs from headers (or creates a correlation ID), puts them in MDC and a thread-local holder,
 * echoes them on the response, then clears context after the request finishes.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    /**
     * Captures or creates request context IDs, exposes them for logging, and cleans up afterward.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response (receives correlation/channel/client headers)
     * @param filterChain the remaining filter chain
     * @throws ServletException if a downstream filter fails with a servlet error
     * @throws IOException      if a downstream filter fails with an I/O error
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = headerOrNew(request, CorrelationConstants.CORRELATION_ID_HEADER);
        String channelId = blankToEmpty(request.getHeader(CorrelationConstants.CHANNEL_ID_HEADER));
        String clientId = blankToEmpty(request.getHeader(CorrelationConstants.CLIENT_ID_HEADER));

        MDC.put(CorrelationConstants.MDC_CORRELATION_ID, correlationId);
        MDC.put(CorrelationConstants.MDC_CHANNEL_ID, channelId);
        MDC.put(CorrelationConstants.MDC_CLIENT_ID, clientId);
        ChannelClientHolder.set(new ChannelClientContext(channelId, clientId, correlationId));

        response.setHeader(CorrelationConstants.CORRELATION_ID_HEADER, correlationId);
        if (!channelId.isBlank()) {
            response.setHeader(CorrelationConstants.CHANNEL_ID_HEADER, channelId);
        }
        if (!clientId.isBlank()) {
            response.setHeader(CorrelationConstants.CLIENT_ID_HEADER, clientId);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            ChannelClientHolder.clear();
            MDC.remove(CorrelationConstants.MDC_CORRELATION_ID);
            MDC.remove(CorrelationConstants.MDC_CHANNEL_ID);
            MDC.remove(CorrelationConstants.MDC_CLIENT_ID);
        }
    }

    /**
     * Returns the trimmed header value, or a new UUID when the header is missing or blank.
     *
     * @param request the HTTP request
     * @param name    header name to read
     * @return existing header value or a newly generated UUID string
     */
    private static String headerOrNew(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? UUID.randomUUID().toString() : value.trim();
    }

    /**
     * Trims a header value, or returns an empty string when the value is null.
     *
     * @param value raw header value (may be null)
     * @return trimmed value, or {@code ""} if null
     */
    private static String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
