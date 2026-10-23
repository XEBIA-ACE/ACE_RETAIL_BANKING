package com.bank.core.notification.dto;

import com.bank.core.notification.domain.DeliveryStatus;
import java.time.Instant;

public record NotificationHistoryItemDto(
        String notificationId,
        String customerId,
        String notificationType,
        String message,
        Instant notificationTimestamp,
        DeliveryStatus deliveryStatus,
        Instant deliveredAt,
        Instant retentionExpiresAt) {
}
