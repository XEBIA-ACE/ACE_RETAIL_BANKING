package com.bank.core.notification.dto;

import java.util.List;

/** Customer notification history, most recent first. An empty list is a normal, non-error result. */
public record NotificationHistoryResponse(List<NotificationHistoryItemDto> notifications) {
}
