package com.cards.payment.web;

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
 * Maps exceptions from payment controllers into standard {@link ErrorResponse} bodies.
 * Delegates formatting to shared common exception-handler support.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorCodeProperties errorCodeProperties;

    /**
     * Handles known business errors and returns the mapped error response.
     *
     * @param ex      the business exception
     * @param request the current HTTP request
     * @return error response with the business error code and status
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return CommonExceptionHandlerSupport.handleBusiness(errorCodeProperties, ex, request.getRequestURI());
    }

    /**
     * Handles bean-validation failures on request bodies.
     *
     * @param ex      the validation exception
     * @param request the current HTTP request
     * @return error response describing the validation problems
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        return CommonExceptionHandlerSupport.handleValidation(errorCodeProperties, ex, request.getRequestURI());
    }

    /**
     * Handles unexpected errors and returns a generic error response.
     *
     * @param ex      the unexpected exception
     * @param request the current HTTP request
     * @return generic unexpected-error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return CommonExceptionHandlerSupport.handleUnexpected(errorCodeProperties, request.getRequestURI());
    }
}
