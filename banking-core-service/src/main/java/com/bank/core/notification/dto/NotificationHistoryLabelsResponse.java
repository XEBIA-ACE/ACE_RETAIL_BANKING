package com.bank.core.notification.dto;

import java.util.List;
import java.util.Map;

/** Localized screen labels, headings and system messages (FR-010). Contains no customer data. */
public record NotificationHistoryLabelsResponse(String locale, List<String> supportedLocales,
        Map<String, String> labels) {
}
