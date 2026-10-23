package com.bank.core.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base for business errors. {@code messageKey} is resolved through the {@link org.springframework.context.MessageSource}
 * so error messages shown to customers are localized.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final String messageKey;

    public BusinessException(HttpStatus status, String code, String messageKey) {
        super(code);
        this.status = status;
        this.code = code;
        this.messageKey = messageKey;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
