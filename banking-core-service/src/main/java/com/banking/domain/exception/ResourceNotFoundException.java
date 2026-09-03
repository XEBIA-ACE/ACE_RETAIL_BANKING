```java
package com.banking.domain.exception;

/**
 * Exception thrown when a requested domain resource cannot be found.
 * Maps to HTTP 404 via {@link com.banking.adapter.in.web.GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code ResourceNotFoundException} with the specified detail message.
     *
     * @param message the detail message describing which resource was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```