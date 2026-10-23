package com.bank.core.notification.domain;

import java.util.Locale;

/** App areas from which a signed-in customer can open the notification history (FR-001). */
public enum NotificationHistoryEntryPoint {
    DASHBOARD("/dashboard"),
    SECURITY_CENTER("/security-center"),
    BILL_PAYMENTS("/bill-payments");

    private final String returnPath;

    NotificationHistoryEntryPoint(String returnPath) {
        this.returnPath = returnPath;
    }

    public String returnPath() {
        return returnPath;
    }

    /** Accepts {@code dashboard}, {@code security-center}, {@code SECURITY_CENTER}, etc. */
    public static NotificationHistoryEntryPoint parse(String value) {
        return valueOf(value.trim().replace('-', '_').toUpperCase(Locale.ROOT));
    }
}
