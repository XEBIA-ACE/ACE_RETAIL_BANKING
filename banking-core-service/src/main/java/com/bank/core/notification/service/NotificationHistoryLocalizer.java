package com.bank.core.notification.service;

import com.bank.core.notification.domain.NotificationCategory;
import com.bank.core.notification.domain.NotificationHistoryEntryPoint;
import com.bank.core.notification.domain.NotificationType;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/** Localizes notification history labels, messages and timestamps (DS-006, FR-010, FR-011). */
@Component
public class NotificationHistoryLocalizer {

    /** Keys the UI renders; every bundle (en, de, fr, it) must define all of them. */
    public static final List<String> UI_LABEL_KEYS = List.of(
            "page.title", "page.heading", "page.intro", "page.skipLink",
            "language.label", "filter.label", "filter.all",
            "filter.TRANSACTION", "filter.BALANCE", "filter.BILL",
            "list.label", "entry.type", "entry.sentAt",
            "action.loadMore", "status.loading", "status.empty",
            "error.loadFailed", "error.signInRequired",
            "back.DASHBOARD", "back.SECURITY_CENTER", "back.BILL_PAYMENTS");

    private final MessageSource messageSource;

    public NotificationHistoryLocalizer(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String message(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }

    public String typeLabel(NotificationType type, Locale locale) {
        return message("type." + type.name(), locale);
    }

    public String categoryLabel(NotificationCategory category, Locale locale) {
        return message("category." + category.name(), locale);
    }

    public String entryPointLinkLabel(NotificationHistoryEntryPoint entryPoint, Locale locale) {
        return message("back." + entryPoint.name(), locale);
    }

    public String formatTimestamp(Instant instant, ZoneId zoneId, Locale locale) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendLocalized(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .appendLiteral(' ')
                .appendZoneText(TextStyle.SHORT)
                .toFormatter(locale);
        return formatter.format(instant.atZone(zoneId));
    }

    public Map<String, String> uiLabels(Locale locale, long retentionDays) {
        Map<String, String> labels = new LinkedHashMap<>();
        for (String key : UI_LABEL_KEYS) {
            labels.put(key, message(key, locale, retentionDays));
        }
        return labels;
    }
}
