package com.cards.auth.web;

import com.cards.common.error.BusinessException;
import com.cards.common.error.ErrorCodeProperties;
import com.cards.common.error.ErrorResponse;
import com.cards.common.web.CommonExceptionHandlerSupport;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps exceptions from auth controllers into standard {@link ErrorResponse} JSON bodies.
 * Delegates formatting to shared common exception-handler helpers and configured error codes.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorCodeProperties errorCodeProperties;

    /**
     * Handles known business errors (for example conflict or unauthorized auth failures).
     *
     * @param ex      the business exception with an error code
     * @param request the current HTTP request (used for the error path)
     * @return an error response with the mapped HTTP status and message
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return CommonExceptionHandlerSupport.handleBusiness(errorCodeProperties, ex, request.getRequestURI());
    }

    /**
     * Handles bean-validation failures on request bodies.
     *
     * @param ex      the validation exception from {@code @Valid}
     * @param request the current HTTP request (used for the error path)
     * @return an error response describing the validation problem
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        return CommonExceptionHandlerSupport.handleValidation(errorCodeProperties, ex, request.getRequestURI());
    }

    /**
     * Handles any unexpected exception as a generic internal error.
     *
     * @param ex      the unexpected exception
     * @param request the current HTTP request (used for the error path)
     * @return a generic unexpected-error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return CommonExceptionHandlerSupport.handleUnexpected(errorCodeProperties, request.getRequestURI());
    }
}
