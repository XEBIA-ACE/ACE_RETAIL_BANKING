package com.bank.core.notification.domain;

import java.util.Arrays;
import java.util.List;

public enum NotificationType {
    LARGE_TRANSACTION(NotificationCategory.TRANSACTION),
    LOW_BALANCE(NotificationCategory.BALANCE),
    BILL_REMINDER(NotificationCategory.BILL);

    private final NotificationCategory category;

    NotificationType(NotificationCategory category) {
        this.category = category;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public static List<NotificationType> forCategory(NotificationCategory category) {
        if (category == null) {
            return List.of(values());
        }
        return Arrays.stream(values()).filter(t -> t.category == category).toList();
    }
}
