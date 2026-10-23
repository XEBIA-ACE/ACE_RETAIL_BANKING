package com.bank.core.notification.domain;

import java.util.Arrays;
import java.util.Optional;

/** Screens from which a signed-in customer can open the notification history (FR-001). */
public enum NotificationHistoryEntryPoint {
    DASHBOARD("dashboard"),
    SECURITY_CENTER("security-center"),
    BILL_PAYMENTS("bill-payments");

    private final String slug;

    NotificationHistoryEntryPoint(String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }

    public String historyUrl() {
        return "/notification-history.html?from=" + slug;
    }

    public static Optional<NotificationHistoryEntryPoint> fromSlug(String slug) {
        return Arrays.stream(values()).filter(e -> e.slug.equalsIgnoreCase(slug)).findFirst();
    }
}
