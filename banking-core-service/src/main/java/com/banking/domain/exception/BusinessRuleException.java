package com.banking.domain.exception;

/**
 * Thrown when a business rule or invariant is violated.
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}