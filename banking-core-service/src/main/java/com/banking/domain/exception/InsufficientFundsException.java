package com.banking.domain.exception;

/**
 * Thrown when an account has insufficient funds for a transaction.
 * Extends BusinessRuleException — maps to HTTP 422 Unprocessable Entity.
 */
public class InsufficientFundsException extends BusinessRuleException {

    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String accountNumber, java.math.BigDecimal required, java.math.BigDecimal available) {
        super(String.format(
                "Insufficient funds in account '%s': required %s, available %s",
                accountNumber, required, available
        ));
    }
}