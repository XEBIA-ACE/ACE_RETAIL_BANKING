package com.bank.core.notification.exception;

import com.bank.core.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidNotificationHistoryRequestException extends BusinessException {

    public InvalidNotificationHistoryRequestException(String code, String messageKey) {
        super(HttpStatus.BAD_REQUEST, code, messageKey);
    }
}
