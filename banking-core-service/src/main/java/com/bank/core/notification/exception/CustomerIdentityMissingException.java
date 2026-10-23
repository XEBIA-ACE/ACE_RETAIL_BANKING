package com.bank.core.notification.exception;

import com.bank.core.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CustomerIdentityMissingException extends BusinessException {

    public CustomerIdentityMissingException() {
        super(HttpStatus.FORBIDDEN, "CUSTOMER_IDENTITY_MISSING", "error.customerIdentityMissing");
    }
}
