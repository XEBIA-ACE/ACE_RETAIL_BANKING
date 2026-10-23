package com.bank.core.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.core.notification.domain.NotificationCategory;
import com.bank.core.notification.domain.NotificationType;
import com.bank.core.notification.service.NotificationHistoryLocalizer;
import com.bank.core.notification.service.SupportedLocales;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** US-028-ac-5: all screen text is localized to en/de/fr/it and timestamps follow locale and time zone. */
class NotificationHistoryLocalizationTest {

    private static final Instant SENT = Instant.parse("2026-01-15T08:05:00Z");

    private final NotificationHistoryLocalizer localizer =
            new NotificationHistoryLocalizer(NotificationHistoryServiceImplTest.messageSource());

    private static Properties bundle(String suffix) throws IOException {
        String path = "/messages/notification-history" + suffix + ".properties";
        try (InputStream in = NotificationHistoryLocalizationTest.class.getResourceAsStream(path)) {
            Properties props = new Properties();
            props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return props;
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"_de", "_fr", "_it"})
    void us028Ac5_everyBundleDefinesEveryKeyWithATranslatedValue(String suffix) throws IOException {
        Properties english = bundle("");
        Properties translated = bundle(suffix);

        assertThat(translated.stringPropertyNames()).isEqualTo(english.stringPropertyNames());
        assertThat(translated.getProperty("page.heading")).isNotEqualTo(english.getProperty("page.heading"));
        assertThat(translated.getProperty("type.LOW_BALANCE")).isNotEqualTo(english.getProperty("type.LOW_BALANCE"));
    }

    @Test
    void us028Ac5_uiLabelsAreLocalizedPerLanguage() {
        Map<String, String> de = localizer.uiLabels(Locale.GERMAN, 90);
        Map<String, String> fr = localizer.uiLabels(Locale.FRENCH, 90);
        Map<String, String> it = localizer.uiLabels(Locale.ITALIAN, 90);
        Map<String, String> en = localizer.uiLabels(Locale.ENGLISH, 90);

        assertThat(de.get("page.heading")).isEqualTo("Benachrichtigungsverlauf");
        assertThat(fr.get("page.heading")).isEqualTo("Historique des notifications");
        assertThat(it.get("page.heading")).isEqualTo("Cronologia delle notifiche");
        assertThat(en.get("page.heading")).isEqualTo("Notification history");
        assertThat(fr.get("error.loadFailed")).contains("n'ont");
        assertThat(de.get("page.intro")).contains("90");
        assertThat(en.keySet()).containsExactlyElementsOf(NotificationHistoryLocalizer.UI_LABEL_KEYS);
    }

    @Test
    void us028Ac5_systemMessagesWithoutArgumentsKeepApostrophes() {
        assertThat(localizer.message("error.invalidTimeZone", Locale.FRENCH))
                .isEqualTo("Le fuseau horaire sélectionné n'est pas pris en charge.");
    }

    @Test
    void us028Ac5_typeAndCategoryLabelsAreLocalized() {
        for (NotificationType type : NotificationType.values()) {
            Set<String> labels = Set.of(localizer.typeLabel(type, Locale.ENGLISH), localizer.typeLabel(type, Locale.GERMAN),
                    localizer.typeLabel(type, Locale.FRENCH), localizer.typeLabel(type, Locale.ITALIAN));
            assertThat(labels).hasSize(4);
        }
        assertThat(localizer.categoryLabel(NotificationCategory.BILL, Locale.ITALIAN)).isEqualTo("Fattura");
    }

    @Test
    void us028Ac5_timestampsUseLocaleSpecificFormatAndTimeZone() {
        ZoneId zurich = ZoneId.of("Europe/Zurich");

        assertThat(localizer.formatTimestamp(SENT, zurich, Locale.GERMAN)).isEqualTo("15.01.2026, 09:05 MEZ");
        assertThat(localizer.formatTimestamp(SENT, zurich, Locale.FRENCH)).startsWith("15 janv. 2026").contains("09:05");
        assertThat(localizer.formatTimestamp(SENT, zurich, Locale.ITALIAN)).startsWith("15 gen 2026").contains("09:05");
        assertThat(localizer.formatTimestamp(SENT, zurich, Locale.ENGLISH)).startsWith("Jan 15, 2026").contains("9:05");
        assertThat(localizer.formatTimestamp(SENT, ZoneId.of("America/New_York"), Locale.ENGLISH))
                .contains("3:05").endsWith("EST");
    }

    @Test
    void us028Ac5_supportedLocaleResolution() {
        assertThat(SupportedLocales.resolve("fr", Locale.GERMAN)).isEqualTo(Locale.FRENCH);
        assertThat(SupportedLocales.resolve("de-CH", Locale.ENGLISH)).isEqualTo(Locale.forLanguageTag("de-CH"));
        assertThat(SupportedLocales.resolve("es", Locale.ITALIAN)).isEqualTo(Locale.ITALIAN);
        assertThat(SupportedLocales.resolve(null, Locale.JAPANESE)).isEqualTo(Locale.ENGLISH);
        assertThat(SupportedLocales.resolve(" ", null)).isEqualTo(Locale.ENGLISH);
    }
}
