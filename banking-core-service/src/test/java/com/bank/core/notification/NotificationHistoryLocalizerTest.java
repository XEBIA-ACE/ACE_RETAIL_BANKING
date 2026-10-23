package com.bank.core.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.core.notification.service.NotificationHistoryLocalizer;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NotificationHistoryLocalizerTest {

    private final NotificationHistoryLocalizer localizer = NotificationHistoryServiceImplTest.localizer();

    /** [US-028-AC5] Every supported language bundle defines exactly the same keys as the English default. */
    @ParameterizedTest
    @ValueSource(strings = {"de", "fr", "it"})
    void us028Ac5_everyBundleIsComplete(String language) throws IOException {
        Properties english = load("messages/notification-history.properties");
        Properties translated = load("messages/notification-history_" + language + ".properties");

        assertThat(translated.stringPropertyNames()).isEqualTo(english.stringPropertyNames());
        translated.stringPropertyNames().forEach(key ->
                assertThat(translated.getProperty(key)).as(language + ":" + key).isNotBlank());
    }

    /** [US-028-AC5] All screen labels, headings and system messages are translated (none fall back to English). */
    @ParameterizedTest
    @ValueSource(strings = {"de", "fr", "it"})
    void us028Ac5_screenLabelsDifferFromEnglish(String language) {
        Map<String, String> english = localizer.screenLabels(Locale.ENGLISH);
        Map<String, String> translated = localizer.screenLabels(Locale.forLanguageTag(language));

        assertThat(translated.keySet()).isEqualTo(english.keySet());
        assertThat(translated).allSatisfy((key, value) -> assertThat(value).as(key).isNotEqualTo(english.get(key)));
    }

    /** [US-028-AC5] Explicit language choice wins over Accept-Language; unsupported/malformed fall back to English. */
    @Test
    void us028Ac5_localeResolution() {
        assertThat(localizer.resolve("de", "fr-CH,fr;q=0.9")).isEqualTo(Locale.GERMAN);
        assertThat(localizer.resolve("it-CH", null)).isEqualTo(Locale.ITALIAN);
        assertThat(localizer.resolve(null, "fr-CH,fr;q=0.9,en;q=0.5")).isEqualTo(Locale.FRENCH);
        assertThat(localizer.resolve("es", "pt-BR")).isEqualTo(Locale.ENGLISH);
        assertThat(localizer.resolve(null, ";;;not-a-range")).isEqualTo(Locale.ENGLISH);
    }

    private static Properties load(String path) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = NotificationHistoryLocalizerTest.class.getClassLoader().getResourceAsStream(path)) {
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return properties;
    }
}
