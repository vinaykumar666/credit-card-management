package com.cards.common.web;

import com.cards.common.error.BusinessException;
import com.cards.common.error.ErrorCodeProperties;
import com.cards.common.error.ErrorCodes;
import com.cards.common.error.ErrorResponse;
import com.cards.common.error.ErrorResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Shared helpers that build standard {@link ErrorResponse} HTTP bodies for common failures.
 * Each service's {@code @RestControllerAdvice} can call these methods so error handling stays thin and consistent.
 */
public final class CommonExceptionHandlerSupport {

    private CommonExceptionHandlerSupport() {
    }

    /**
     * Builds an error response from a {@link BusinessException} using the configured error catalog.
     *
     * @param properties loaded error-code definitions (status and default message)
     * @param ex         the business exception that was thrown
     * @param path       request path to include in the response body
     * @return a {@link ResponseEntity} with the mapped HTTP status and error body
     */
    public static ResponseEntity<ErrorResponse> handleBusiness(ErrorCodeProperties properties,
                                                               BusinessException ex,
                                                               String path) {
        ErrorResponse body = ErrorResponseFactory.from(properties, ex, path);
        return ResponseEntity.status(body.status()).body(body);
    }

    /**
     * Builds an error response for bean-validation failures ({@code @Valid} request bodies).
     * Uses {@link ErrorCodes#COMMON_001} and replaces the message with the first field error detail.
     *
     * @param properties loaded error-code definitions
     * @param ex         Spring validation exception with field errors
     * @param path       request path to include in the response body
     * @return a {@link ResponseEntity} with HTTP 400 (via the catalog) and a field-level message
     */
    public static ResponseEntity<ErrorResponse> handleValidation(ErrorCodeProperties properties,
                                                                 MethodArgumentNotValidException ex,
                                                                 String path) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(CommonExceptionHandlerSupport::formatField)
                .orElse("Validation failed");
        ErrorResponse base = ErrorResponseFactory.from(properties, ErrorCodes.COMMON_001, path);
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(base.timestamp())
                .status(base.status())
                .errorCode(base.errorCode())
                .message(detail)
                .path(base.path())
                .correlationId(base.correlationId())
                .channelId(base.channelId())
                .clientId(base.clientId())
                .build();
        return ResponseEntity.status(body.status()).body(body);
    }

    /**
     * Builds a generic unexpected-error response using {@link ErrorCodes#COMMON_002}.
     *
     * @param properties loaded error-code definitions
     * @param path       request path to include in the response body
     * @return a {@link ResponseEntity} with HTTP 500 (via the catalog) and a standard message
     */
    public static ResponseEntity<ErrorResponse> handleUnexpected(ErrorCodeProperties properties, String path) {
        ErrorResponse body = ErrorResponseFactory.from(properties, ErrorCodes.COMMON_002, path);
        return ResponseEntity.status(body.status()).body(body);
    }

    /**
     * Formats a single validation field error as {@code field: message}.
     *
     * @param err Spring field error
     * @return human-readable field error text
     */
    private static String formatField(FieldError err) {
        return err.getField() + ": " + err.getDefaultMessage();
    }
}
