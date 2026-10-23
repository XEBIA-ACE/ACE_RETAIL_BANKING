package com.bank.core.notification.dto;

import java.util.List;

public record NotificationHistoryResponse(
        String locale,
        String timeZone,
        long retentionDays,
        String entryPoint,
        String returnPath,
        String returnLabel,
        List<NotificationHistoryItemDto> notifications) {
}
