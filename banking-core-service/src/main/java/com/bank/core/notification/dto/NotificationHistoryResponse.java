package com.bank.core.notification.dto;

import java.util.List;

public record NotificationHistoryResponse(
        List<NotificationHistoryItemDto> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        long retentionDays,
        String locale,
        String timeZone,
        String entryPoint,
        String summary) {
}
