package com.banking.adapter.in.web;

import com.banking.domain.exception.BusinessRuleException;
import com.banking.domain.exception.InsufficientFundsException;
import com.banking.domain.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the Banking Core Service REST API.
 * Translates domain and application exceptions into structured HTTP error responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // Error response record
    // -------------------------------------------------------------------------

    /**
     * Standard error response body returned for all handled exceptions.
     */
    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp
    ) {
        static ErrorResponse of(HttpStatus httpStatus, String message) {
            return new ErrorResponse(
                    httpStatus.value(),
                    httpStatus.getReasonPhrase(),
                    message,
                    LocalDateTime.now()
            );
        }
    }

    /**
     * Validation error response body — extends the standard body with per-field details.
     */
    public record ValidationErrorResponse(
            int status,
            String error,
            String message,
            Map<String, String> fieldErrors,
            LocalDateTime timestamp
    ) {
        static ValidationErrorResponse of(HttpStatus httpStatus, String message, Map<String, String> fieldErrors) {
            return new ValidationErrorResponse(
                    httpStatus.value(),
                    httpStatus.getReasonPhrase(),
                    message,
                    fieldErrors,
                    LocalDateTime.now()
            );
        }
    }

    // -------------------------------------------------------------------------
    // 404 — Resource Not Found
    // -------------------------------------------------------------------------

    /**
     * Handles {@link ResourceNotFoundException}: returns HTTP 404 with a JSON error body
     * containing at minimum a {@code message} field.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        ErrorResponse body = ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // -------------------------------------------------------------------------
    // 422 — Business Rule Violation
    // -------------------------------------------------------------------------

    /**
     * Handles {@link BusinessRuleException} (and its subclasses, e.g.
     * {@link InsufficientFundsException}): returns HTTP 422 with a JSON error body
     * containing at minimum a {@code message} field.
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleException(BusinessRuleException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        ErrorResponse body = ErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    // -------------------------------------------------------------------------
    // 400 — Validation Errors (Bean Validation)
    // -------------------------------------------------------------------------

    /**
     * Handles {@link MethodArgumentNotValidException} thrown when {@code @Valid} fails:
     * returns HTTP 400 with a structured body listing per-field validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null
                                ? fieldError.getDefaultMessage()
                                : "Invalid value",
                        // keep first error per field if there are multiple
                        (existing, replacement) -> existing
                ));

        log.warn("Validation failed: {}", fieldErrors);

        ValidationErrorResponse body = ValidationErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // -------------------------------------------------------------------------
    // 400 — Illegal Argument
    // -------------------------------------------------------------------------

    /**
     * Handles {@link IllegalArgumentException}: returns HTTP 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // -------------------------------------------------------------------------
    // 500 — Unexpected Errors
    // -------------------------------------------------------------------------

    /**
     * Catch-all handler for any unhandled exception: returns HTTP 500.
     * The response deliberately omits internal details to avoid information leakage.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}