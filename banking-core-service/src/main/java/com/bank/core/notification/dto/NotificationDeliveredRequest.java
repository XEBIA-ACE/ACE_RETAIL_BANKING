package com.bank.core.notification.dto;

import com.bank.core.notification.domain.DeliveryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Payload sent by the Smart Notifications capability once delivery processing completes (FR-003). */
public record NotificationDeliveredRequest(
        @NotBlank @Size(max = 36) String customerId,
        @NotBlank @Pattern(regexp = "^[A-Z][A-Z0-9_]{0,39}$",
                message = "must be an upper-case identifier such as LARGE_TRANSACTION") String notificationType,
        @NotBlank @Size(max = 1000) String message,
        @NotNull Instant notificationTimestamp,
        @NotNull DeliveryStatus deliveryStatus,
        @NotNull Instant deliveredAt) {
}
