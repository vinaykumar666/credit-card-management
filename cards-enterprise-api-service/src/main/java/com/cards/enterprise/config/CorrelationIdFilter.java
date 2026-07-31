package com.cards.enterprise.config;

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
 * Early servlet filter that sets correlation, channel, and client ids on every request.
 * Puts values into MDC and {@link ChannelClientHolder}, echoes them on the response, then clears them.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    /**
     * Captures or creates tracking headers, stores them for the request, then cleans up afterward.
     *
     * @param request     incoming HTTP request
     * @param response    outgoing HTTP response
     * @param filterChain remaining filter chain
     * @throws ServletException if the chain raises a servlet error
     * @throws IOException      if an I/O error occurs while filtering
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
     * Reads a header value or generates a new UUID when the header is missing or blank.
     *
     * @param request HTTP request
     * @param name    header name
     * @return trimmed header value or a new UUID string
     */
    private static String headerOrNew(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? UUID.randomUUID().toString() : value.trim();
    }

    /**
     * Converts null to empty string and trims non-null values.
     *
     * @param value raw header value
     * @return trimmed value or empty string
     */
    private static String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
