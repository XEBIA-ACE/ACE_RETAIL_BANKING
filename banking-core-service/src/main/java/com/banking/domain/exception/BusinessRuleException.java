package com.banking.domain.exception;

/**
 * Thrown when a business rule is violated.
 * Maps to HTTP 400 via {@link com.banking.adapter.in.web.GlobalExceptionHandler}.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
