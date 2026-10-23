package com.bank.core.notification.service;

import com.bank.core.notification.dto.NotificationHistoryLabelsResponse;
import com.bank.core.notification.dto.NotificationHistoryQuery;
import com.bank.core.notification.dto.NotificationHistoryResponse;
import java.time.Instant;
import java.util.Locale;

public interface NotificationHistoryService {

    /** The authenticated customer's notifications within the retention window, newest first. */
    NotificationHistoryResponse getHistory(NotificationHistoryQuery query);

    NotificationHistoryLabelsResponse getLabels(Locale locale);

    /** Oldest instant still inside the retention window; shared by reads and the purge job (FR-007). */
    Instant retentionCutoff();

    /** Deletes notification logs that fall outside the retention window. */
    int purgeExpired();
}
