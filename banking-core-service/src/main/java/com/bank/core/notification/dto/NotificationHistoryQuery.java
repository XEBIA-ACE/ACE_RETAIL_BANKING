package com.bank.core.notification.dto;

import com.bank.core.notification.domain.NotificationHistoryEntryPoint;
import com.bank.core.notification.domain.NotificationType;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;

/** Validated history request. {@code customerId} always comes from the authenticated token (FR-008). */
public record NotificationHistoryQuery(
        String customerId,
        Set<NotificationType> types,
        Locale locale,
        ZoneId timeZone,
        NotificationHistoryEntryPoint entryPoint) {
}
