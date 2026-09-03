```java
package com.banking.domain.exception;

/**
 * Exception thrown when a domain business rule is violated.
 * Maps to HTTP 422 via {@link com.banking.adapter.in.web.GlobalExceptionHandler}.
 */
public class BusinessRuleException extends RuntimeException {

    /**
     * Constructs a new {@code BusinessRuleException} with the specified detail message.
     *
     * @param message the detail message describing the violated business rule
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}
```