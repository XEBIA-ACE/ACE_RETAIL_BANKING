package com.banking.domain.exception;

/**
 * Thrown when a debit or payment operation is attempted but the source account
 * does not have sufficient available balance to cover the requested amount.
 * Mapped to HTTP 422 Unprocessable Entity by
 * {@link com.banking.adapter.in.web.GlobalExceptionHandler}.
 */
public class InsufficientFundsException extends RuntimeException {

    /**
     * Constructs a new InsufficientFundsException with the given detail message.
     *
     * @param message human-readable description of the shortfall
     */
    public InsufficientFundsException(String message) {
        super(message);
    }
}
