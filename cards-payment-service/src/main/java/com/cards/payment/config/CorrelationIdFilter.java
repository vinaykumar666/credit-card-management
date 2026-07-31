package com.cards.payment.config;

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
 * Servlet filter that sets correlation, channel, and client ids for each request.
 * Reads them from headers when present, otherwise creates a new correlation id,
 * and clears MDC and channel context after the request.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    /**
     * Propagates tracing headers into MDC and the channel-client holder for the request.
     *
     * @param request     incoming HTTP request
     * @param response    HTTP response (also receives echo headers)
     * @param filterChain remaining filter chain
     * @throws ServletException if the chain throws a servlet exception
     * @throws IOException      if the chain throws an I/O exception
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
     * Returns a trimmed header value, or a new UUID string when missing or blank.
     *
     * @param request HTTP request
     * @param name    header name
     * @return existing header value or a new UUID
     */
    private static String headerOrNew(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? UUID.randomUUID().toString() : value.trim();
    }

    /**
     * Trims a string, treating null as empty.
     *
     * @param value raw header value
     * @return trimmed value, or empty string when null
     */
    private static String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
