package com.bank.core.notification.dto;

import com.bank.core.notification.domain.NotificationCategory;
import com.bank.core.notification.domain.NotificationHistoryEntryPoint;
import java.time.ZoneId;
import java.util.Locale;

/** Validated history request. {@code customerId} always comes from the authenticated principal. */
public record NotificationHistoryQuery(
        Long customerId,
        NotificationCategory category,
        NotificationHistoryEntryPoint entryPoint,
        Locale locale,
        ZoneId zoneId,
        int page,
        int size) {
}
