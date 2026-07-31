package com.cards.bff.web;

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
 * Maps exceptions from BFF controllers into consistent {@link ErrorResponse} JSON bodies.
 * Delegates formatting to shared exception-handler support in {@code cards-common}.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorCodeProperties errorCodeProperties;

    /**
     * Handles known business errors (including validation and downstream failures).
     *
     * @param ex      business exception
     * @param request HTTP request used for the error path
     * @return error response with the mapped status and body
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return CommonExceptionHandlerSupport.handleBusiness(errorCodeProperties, ex, request.getRequestURI());
    }

    /**
     * Handles bean-validation failures on request bodies.
     *
     * @param ex      Spring validation exception
     * @param request HTTP request used for the error path
     * @return error response describing validation problems
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        return CommonExceptionHandlerSupport.handleValidation(errorCodeProperties, ex, request.getRequestURI());
    }

    /**
     * Handles unexpected exceptions that were not mapped elsewhere.
     *
     * @param ex      unexpected exception
     * @param request HTTP request used for the error path
     * @return generic unexpected-error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return CommonExceptionHandlerSupport.handleUnexpected(errorCodeProperties, request.getRequestURI());
    }
}
