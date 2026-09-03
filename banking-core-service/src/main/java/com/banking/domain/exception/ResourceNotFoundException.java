package com.banking.domain.exception;

/**
 * Thrown when a requested resource cannot be found.
 * Maps to HTTP 404 via {@link com.banking.adapter.in.web.GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
