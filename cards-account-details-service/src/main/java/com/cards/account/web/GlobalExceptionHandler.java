package com.cards.account.web;

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
 * Central REST exception handler for the account details service.
 * Turns business, validation, and unexpected errors into standard {@link ErrorResponse} bodies.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorCodeProperties errorCodeProperties;

    /**
     * Handles known business exceptions with their mapped HTTP status and error code.
     *
     * @param ex      business exception thrown by the service layer
     * @param request current HTTP request
     * @return error response with the business error details
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return CommonExceptionHandlerSupport.handleBusiness(errorCodeProperties, ex, request.getRequestURI());
    }

    /**
     * Handles bean-validation failures on request bodies.
     *
     * @param ex      validation exception from Spring MVC
     * @param request current HTTP request
     * @return error response describing the validation problems
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        return CommonExceptionHandlerSupport.handleValidation(errorCodeProperties, ex, request.getRequestURI());
    }

    /**
     * Handles any unexpected exception as a generic server error.
     *
     * @param ex      unexpected exception
     * @param request current HTTP request
     * @return generic unexpected-error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return CommonExceptionHandlerSupport.handleUnexpected(errorCodeProperties, request.getRequestURI());
    }
}
