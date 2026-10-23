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

/**
 * Localization for the notification history screen (DS-006, FR-010, FR-011). Uses the Spring
 * {@link MessageSource} bundles under {@code messages/notification-history*.properties}.
 */
@Component
public class NotificationHistoryLocalizer {

    public static final List<Locale> SUPPORTED_LOCALES =
            List.of(Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH, Locale.ITALIAN);

    static final List<String> SCREEN_KEYS = List.of(
            "history.title", "history.intro", "history.skip", "history.retention", "history.timezone",
            "history.language.label", "history.token.label", "history.token.submit", "history.filter.legend",
            "history.filter.apply", "history.list.label", "history.count", "history.timestamp.label",
            "history.loading", "history.empty", "history.error.session", "history.error.generic",
            "history.error.invalidRequest");

    private final MessageSource messages;

    public NotificationHistoryLocalizer(MessageSource messages) {
        this.messages = messages;
    }

    /** Explicit language choice wins, then Accept-Language, then English. */
    public Locale resolve(String language, String acceptLanguage) {
        Locale chosen = match(language);
        if (chosen != null) {
            return chosen;
        }
        if (acceptLanguage != null && !acceptLanguage.isBlank()) {
            try {
                Locale lookup = Locale.lookup(Locale.LanguageRange.parse(acceptLanguage), SUPPORTED_LOCALES);
                if (lookup != null) {
                    return lookup;
                }
            } catch (IllegalArgumentException ignored) {
                // Malformed header: fall back to the default language.
            }
        }
        return Locale.ENGLISH;
    }

    public Map<String, String> screenLabels(Locale locale) {
        Map<String, String> labels = new LinkedHashMap<>();
        SCREEN_KEYS.forEach(key -> labels.put(key, message(key, locale)));
        for (NotificationType type : NotificationType.values()) {
            labels.put("type." + type.name(), typeLabel(type, locale));
        }
        for (NotificationCategory category : NotificationCategory.values()) {
            labels.put("category." + category.name(), categoryLabel(category, locale));
        }
        for (NotificationHistoryEntryPoint entryPoint : NotificationHistoryEntryPoint.values()) {
            labels.put("entry." + entryPoint.name(), returnLabel(entryPoint, locale));
        }
        return labels;
    }

    public String typeLabel(NotificationType type, Locale locale) {
        return message("type." + type.name(), locale);
    }

    public String categoryLabel(NotificationCategory category, Locale locale) {
        return message("category." + category.name(), locale);
    }

    public String returnLabel(NotificationHistoryEntryPoint entryPoint, Locale locale) {
        return message("entry." + entryPoint.name(), locale);
    }

    /** Locale-specific date and time, with the time zone name, e.g. {@code 23.10.2026, 14:05 MESZ}. */
    public String formatTimestamp(Instant instant, Locale locale, ZoneId zone) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendLocalized(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .appendLiteral(' ')
                .appendZoneText(TextStyle.SHORT)
                .toFormatter(locale)
                .withZone(zone);
        return formatter.format(instant);
    }

    private String message(String key, Locale locale) {
        return messages.getMessage(key, null, locale);
    }

    private static Locale match(String language) {
        if (language == null || language.isBlank()) {
            return null;
        }
        String tag = Locale.forLanguageTag(language.trim().replace('_', '-')).getLanguage();
        return SUPPORTED_LOCALES.stream().filter(l -> l.getLanguage().equals(tag)).findFirst().orElse(null);
    }
}
