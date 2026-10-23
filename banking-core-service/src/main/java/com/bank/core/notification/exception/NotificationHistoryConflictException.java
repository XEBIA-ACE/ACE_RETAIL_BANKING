package com.bank.core.notification.exception;

public class NotificationHistoryConflictException extends RuntimeException {

    public NotificationHistoryConflictException(String sourceNotificationId) {
        super("Notification " + sourceNotificationId + " is already recorded for a different customer");
    }
}
