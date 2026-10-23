package com.bank.core.notification.dto;

import com.bank.core.notification.domain.NotificationCategory;
import com.bank.core.notification.domain.NotificationType;
import java.time.OffsetDateTime;

public record NotificationHistoryItemDto(
        String id,
        NotificationType type,
        String typeLabel,
        NotificationCategory category,
        String categoryLabel,
        String description,
        OffsetDateTime occurredAt,
        String occurredAtFormatted) {
}
