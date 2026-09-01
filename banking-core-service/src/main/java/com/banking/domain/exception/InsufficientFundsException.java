package com.banking.domain.exception;

/**
 * Thrown when an account has insufficient funds for a transaction.
 */
public class InsufficientFundsException extends BusinessRuleException {

    public InsufficientFundsException(String accountId) {
        super("Insufficient funds in account: " + accountId);
    }
}
