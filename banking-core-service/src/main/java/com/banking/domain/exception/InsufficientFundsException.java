```java
package com.banking.domain.exception;

/**
 * Exception thrown when an account does not have sufficient funds to complete an operation.
 * Maps to HTTP 422 via {@link com.banking.adapter.in.web.GlobalExceptionHandler}.
 */
public class InsufficientFundsException extends RuntimeException {

    /**
     * Constructs a new {@code InsufficientFundsException} with the specified detail message.
     *
     * @param message the detail message describing the insufficient funds condition
     */
    public InsufficientFundsException(String message) {
        super(message);
    }
}
```