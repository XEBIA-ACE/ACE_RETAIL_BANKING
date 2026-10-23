package com.bank.core.notification.domain;

/** Notification types shown in the history (FR-004). */
public enum NotificationType {
    LARGE_TRANSACTION(NotificationCategory.TRANSACTION),
    LOW_BALANCE(NotificationCategory.BALANCE),
    BILL_REMINDER(NotificationCategory.BILL);

    private final NotificationCategory category;

    NotificationType(NotificationCategory category) {
        this.category = category;
    }

    public NotificationCategory category() {
        return category;
    }
}
