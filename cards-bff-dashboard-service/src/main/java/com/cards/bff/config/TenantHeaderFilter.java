package com.cards.bff.config;

import com.cards.common.channel.ChannelClientContext;
import com.cards.common.channel.ChannelClientHolder;
import com.cards.common.correlation.CorrelationConstants;
import com.cards.common.error.ErrorCodeProperties;
import com.cards.common.error.ErrorCodes;
import com.cards.common.error.ErrorResponse;
import com.cards.common.error.ErrorResponseFactory;
import com.cards.common.error.ValidationBusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates and stores channel and client tenant headers on each request.
 * Rejects missing or unknown values with a structured error response, then sets MDC and {@link ChannelClientHolder}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class TenantHeaderFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final AppProperties appProperties;
    private final ErrorCodeProperties errorCodeProperties;
    private final ObjectMapper objectMapper;

    /**
     * Skips tenant checks for actuator and OpenAPI/Swagger paths.
     *
     * @param request current HTTP request
     * @return {@code true} when this filter should not run
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Browser CORS preflight never includes custom app headers — must not reject OPTIONS.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return PATH_MATCHER.match("/actuator/**", path)
                || PATH_MATCHER.match("/v3/api-docs/**", path)
                || PATH_MATCHER.match("/swagger-ui/**", path)
                || PATH_MATCHER.match("/swagger-ui.html", path);
    }

    /**
     * Checks channel/client headers against allow-lists, then continues the chain with tenant context set.
     *
     * @param request     current HTTP request
     * @param response    current HTTP response
     * @param filterChain remaining filters
     * @throws ServletException if the chain fails with a servlet error
     * @throws IOException      if an I/O error occurs while writing an error or processing the request
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String channelId = blankToNull(request.getHeader(CorrelationConstants.CHANNEL_ID_HEADER));
        String clientId = blankToNull(request.getHeader(CorrelationConstants.CLIENT_ID_HEADER));
        String correlationId = MDC.get(CorrelationConstants.MDC_CORRELATION_ID);

        try {
            if (channelId == null) {
                writeError(response, new ValidationBusinessException(ErrorCodes.BFF_001), request.getRequestURI());
                return;
            }
            if (clientId == null) {
                writeError(response, new ValidationBusinessException(ErrorCodes.BFF_002), request.getRequestURI());
                return;
            }

            Set<String> allowedChannels = normalizeUpper(appProperties.channels().allowed());
            Set<String> allowedClients = normalize(appProperties.clients().allowed());
            String normalizedChannel = channelId.toUpperCase(Locale.ROOT);

            if (!allowedChannels.contains(normalizedChannel)) {
                writeError(response, new ValidationBusinessException(ErrorCodes.BFF_003), request.getRequestURI());
                return;
            }
            if (!allowedClients.contains(clientId)) {
                writeError(response, new ValidationBusinessException(ErrorCodes.BFF_004), request.getRequestURI());
                return;
            }

            MDC.put(CorrelationConstants.MDC_CHANNEL_ID, normalizedChannel);
            MDC.put(CorrelationConstants.MDC_CLIENT_ID, clientId);
            ChannelClientHolder.set(new ChannelClientContext(normalizedChannel, clientId, correlationId));

            response.setHeader(CorrelationConstants.CHANNEL_ID_HEADER, normalizedChannel);
            response.setHeader(CorrelationConstants.CLIENT_ID_HEADER, clientId);

            filterChain.doFilter(request, response);
        } finally {
            ChannelClientHolder.clear();
            MDC.remove(CorrelationConstants.MDC_CHANNEL_ID);
            MDC.remove(CorrelationConstants.MDC_CLIENT_ID);
        }
    }

    /**
     * Writes a JSON {@link ErrorResponse} for a validation failure and sets the HTTP status.
     *
     * @param response response to write to
     * @param ex       validation exception carrying the error code
     * @param path     request path included in the error body
     * @throws IOException if serialization or writing fails
     */
    private void writeError(HttpServletResponse response, ValidationBusinessException ex, String path)
            throws IOException {
        ErrorResponse body = ErrorResponseFactory.from(errorCodeProperties, ex, path);
        response.setStatus(body.status());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    /**
     * Trims and collects non-blank values into an unmodifiable set (case preserved).
     *
     * @param values raw configured list
     * @return normalized set of values
     */
    private static Set<String> normalize(java.util.List<String> values) {
        return values.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Trims and uppercases non-blank values into an unmodifiable set.
     *
     * @param values raw configured list
     * @return normalized uppercase set of values
     */
    private static Set<String> normalizeUpper(java.util.List<String> values) {
        return values.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(v -> v.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Converts blank or null strings to {@code null}; otherwise returns the trimmed value.
     *
     * @param value raw header value
     * @return trimmed value, or {@code null} if missing/blank
     */
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
