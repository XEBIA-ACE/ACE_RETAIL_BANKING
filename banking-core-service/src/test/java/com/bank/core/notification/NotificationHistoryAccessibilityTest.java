package com.bank.core.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.core.notification.service.NotificationHistoryLocalizer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Static WCAG 2.1 AA checks of the notification history screen (US-028-ac-6, DS-007). */
class NotificationHistoryAccessibilityTest {

    private static Document html;
    private static String css;
    private static String js;
    private static final Map<String, String> COLOURS = new HashMap<>();

    private static String resource(String path) throws IOException {
        try (InputStream in = NotificationHistoryAccessibilityTest.class.getResourceAsStream(path)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @BeforeAll
    static void load() throws IOException {
        html = Jsoup.parse(resource("/static/notification-history.html"));
        css = resource("/static/css/notification-history.css");
        js = resource("/static/js/notification-history.js");
        Matcher m = Pattern.compile("--(nh-[a-z-]+):\\s*(#[0-9a-fA-F]{6})").matcher(css);
        while (m.find()) {
            COLOURS.put(m.group(1), m.group(2));
        }
    }

    private static double luminance(String hex) {
        double[] rgb = new double[3];
        for (int i = 0; i < 3; i++) {
            double c = Integer.parseInt(hex.substring(1 + 2 * i, 3 + 2 * i), 16) / 255.0;
            rgb[i] = c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
        }
        return 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2];
    }

    static double contrast(String a, String b) {
        double l1 = luminance(a);
        double l2 = luminance(b);
        return (Math.max(l1, l2) + 0.05) / (Math.min(l1, l2) + 0.05);
    }

    @ParameterizedTest(name = "US-028-AC6 contrast {0} on {1} >= {2}")
    @CsvSource({
        "nh-text, nh-bg, 4.5", "nh-text, nh-surface, 4.5", "nh-text-muted, nh-bg, 4.5",
        "nh-text-muted, nh-surface, 4.5", "nh-link, nh-bg, 4.5", "nh-error, nh-bg, 4.5",
        "nh-error, nh-error-bg, 4.5", "nh-button-fg, nh-button-bg, 4.5",
        "nh-transaction-fg, nh-transaction-bg, 4.5", "nh-balance-fg, nh-balance-bg, 4.5",
        "nh-bill-fg, nh-bill-bg, 4.5", "nh-focus, nh-bg, 3.0", "nh-border, nh-bg, 3.0"
    })
    void us028Ac6_colourPairsMeetContrastMinimum(String fg, String bg, double minimum) {
        assertThat(COLOURS).containsKeys(fg, bg);
        assertThat(contrast(COLOURS.get(fg), COLOURS.get(bg))).isGreaterThanOrEqualTo(minimum);
    }

    @Test
    void us028Ac6_documentHasLanguageTitleLandmarksAndSingleHeading() {
        assertThat(html.selectFirst("html").attr("lang")).isNotBlank();
        assertThat(html.title()).isNotBlank();
        assertThat(html.select("main")).hasSize(1);
        assertThat(html.select("h1")).hasSize(1);
        String skipTarget = html.selectFirst("a.skip-link").attr("href").substring(1);
        assertThat(html.getElementById(skipTarget)).isNotNull();
    }

    @Test
    void us028Ac6_everyControlHasAnAccessibleLabel() {
        for (Element control : html.select("select, input")) {
            assertThat(html.select("label[for=" + control.id() + "]")).as(control.id()).hasSize(1);
        }
        for (Element button : html.select("button")) {
            assertThat(button.text()).isNotBlank();
        }
    }

    @Test
    void us028Ac6_ariaReferencesResolveAndStatusChangesAreAnnounced() {
        for (Element el : html.select("[aria-labelledby]")) {
            for (String id : el.attr("aria-labelledby").split("\\s+")) {
                assertThat(html.getElementById(id)).as(id).isNotNull();
            }
        }
        assertThat(html.select("[role=status][aria-live=polite]")).hasSize(1);
        assertThat(html.select("[role=alert]")).hasSize(1);
    }

    @Test
    void us028Ac6_zoomIsNotDisabledAndTextUsesRelativeUnits() {
        String viewport = html.selectFirst("meta[name=viewport]").attr("content");
        assertThat(viewport).doesNotContain("user-scalable=no").doesNotContain("maximum-scale");
        assertThat(css).doesNotContainPattern("font-size:\\s*[0-9.]+px");
        assertThat(css).doesNotContainPattern("(?<!-)(width|height):\\s*[0-9]{2,}px");
        assertThat(css).contains("flex-wrap: wrap").contains("overflow-wrap");
    }

    @Test
    void us028Ac6_keyboardFocusIsVisibleAndUserPreferencesAreRespected() {
        assertThat(css).contains(":focus-visible").containsPattern("outline:\\s*0\\.1875rem solid");
        assertThat(css).contains("@media (prefers-reduced-motion: reduce)").contains("@media (forced-colors: active)");
        assertThat(js).contains("li.tabIndex = 0").contains("first.focus()");
    }

    @Test
    void us028Ac2_typesAreDistinguishedByColourAndByText() {
        assertThat(css).contains(".nh-entry--transaction").contains(".nh-entry--balance").contains(".nh-entry--bill");
        assertThat(java.util.Set.of(COLOURS.get("nh-transaction-fg"), COLOURS.get("nh-balance-fg"),
                COLOURS.get("nh-bill-fg"))).hasSize(3);
        assertThat(js).contains("'nh-entry nh-entry--' + category")
                .contains("item.typeLabel + ' (' + item.categoryLabel + ')'")
                .contains("icon.setAttribute('aria-hidden', 'true')");
    }

    @Test
    void us028Ac1_entriesRenderTypeDescriptionAndMachineReadableTime() {
        assertThat(js).contains("desc.textContent = item.description")
                .contains("time.dateTime = item.occurredAt")
                .contains("item.occurredAtFormatted");
    }

    @Test
    void us028Ac4_screenNeverSendsACustomerIdentifier() {
        assertThat(js).contains("'Bearer ' + token").doesNotContainPattern("customer_?[iI]d\\s*[:=]");
        assertThat(js).doesNotContain("innerHTML");
    }

    @Test
    void us028Ac5_everyTranslatableElementUsesAKnownLabelKey() {
        for (Element el : html.select("[data-i18n]")) {
            assertThat(NotificationHistoryLocalizer.UI_LABEL_KEYS).contains(el.attr("data-i18n"));
        }
        assertThat(html.select("#language-select option").eachAttr("value")).containsExactly("en", "de", "fr", "it");
    }
}
