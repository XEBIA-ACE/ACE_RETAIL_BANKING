package com.bank.core.notification.service;

import java.util.List;
import java.util.Locale;

/** Languages supported by the notification history screen (FR-010). English is the fallback. */
public final class SupportedLocales {

    public static final List<String> LANGUAGES = List.of("en", "de", "fr", "it");

    private SupportedLocales() {
    }

    public static Locale resolve(String requestedTag, Locale requestLocale) {
        Locale requested = requestedTag == null || requestedTag.isBlank() ? null : Locale.forLanguageTag(requestedTag);
        if (isSupported(requested)) {
            return requested;
        }
        if (isSupported(requestLocale)) {
            return requestLocale;
        }
        return Locale.ENGLISH;
    }

    private static boolean isSupported(Locale locale) {
        return locale != null && LANGUAGES.contains(locale.getLanguage());
    }
}
