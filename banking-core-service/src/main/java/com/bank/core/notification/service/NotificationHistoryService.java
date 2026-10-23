package com.bank.core.notification.service;

import com.bank.core.notification.dto.NotificationHistoryLabelsResponse;
import com.bank.core.notification.dto.NotificationHistoryQuery;
import com.bank.core.notification.dto.NotificationHistoryResponse;
import java.util.Locale;

public interface NotificationHistoryService {

    NotificationHistoryResponse getHistory(NotificationHistoryQuery query);

    NotificationHistoryLabelsResponse getLabels(Locale locale);
}
