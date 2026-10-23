package com.bank.core.notification.service;

import com.bank.core.notification.dto.NotificationDeliveredRequest;
import com.bank.core.notification.dto.NotificationHistoryResponse;
import com.bank.core.notification.dto.RecordNotificationResult;

public interface NotificationHistoryService {

    /** FR-001 / FR-002: the customer's history, most recent first; empty when none exists. */
    NotificationHistoryResponse getHistory(String customerId);

    /** FR-003: idempotently persists a notification whose delivery processing has completed. */
    RecordNotificationResult recordDelivered(String sourceNotificationId, NotificationDeliveredRequest request);
}
