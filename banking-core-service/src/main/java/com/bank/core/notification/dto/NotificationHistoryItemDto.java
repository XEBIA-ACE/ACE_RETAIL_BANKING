package com.bank.core.notification.dto;

import com.bank.core.notification.domain.NotificationCategory;
import com.bank.core.notification.domain.NotificationType;
import java.time.Instant;

/** One history entry: type indicator, high-level description and localized timestamp (FR-003 - FR-005). */
public record NotificationHistoryItemDto(
        String notificationId,
        NotificationType notificationType,
        NotificationCategory category,
        String typeLabel,
        String categoryLabel,
        String description,
        Instant timestamp,
        String formattedTimestamp) {
}
