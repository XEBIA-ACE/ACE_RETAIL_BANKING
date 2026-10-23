package com.bank.core.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * [US-028-AC6] Obligation ad7cb7df-59fa-59f0-96ff-d061d6d89412:test:characterization:US-028-ac-6 (T021).
 * Static WCAG 2.1 AA checks on the notification history screen: labelled controls, landmarks, live regions,
 * keyboard reachability, colour contrast and text-resize-safe sizing.
 */
class NotificationHistoryAccessibilityTest {

    private static final Pattern COLOR_VAR = Pattern.compile("--nh-([a-z-]+?)-(fg|bg):\\s*#([0-9a-fA-F]{6})");

    private static String resource(String path) throws IOException {
        try (InputStream in = NotificationHistoryAccessibilityTest.class.getClassLoader().getResourceAsStream(path)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** [US-028-AC6] Page declares a language, landmarks, heading and a skip link to the list. */
    @Test
    void us028Ac6_pageHasLanguageLandmarksAndSkipLink() throws IOException {
        String html = resource("static/notification-history.html");

        assertThat(html).contains("<html lang=\"en\">")
                .contains("<main class=\"history\"")
                .contains("<h1 id=\"history-title\"")
                .contains("class=\"skip-link\" href=\"#notification-list\"")
                .contains("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .doesNotContain("user-scalable=no")
                .doesNotContain("maximum-scale");
    }

    /** [US-028-AC6] Every form control has a programmatic label; filters are grouped in a fieldset/legend. */
    @Test
    void us028Ac6_everyControlIsLabelled() throws IOException {
        String html = resource("static/notification-history.html");
        Matcher ids = Pattern.compile("<(?:input|select)[^>]*\\sid=\"([^\"]+)\"").matcher(html);
        int controls = 0;
        while (ids.find()) {
            controls++;
            assertThat(html).as("label for " + ids.group(1)).contains("<label for=\"" + ids.group(1) + "\"");
        }
        assertThat(controls).isEqualTo(5);
        assertThat(html).contains("<fieldset class=\"filters\">").contains("<legend");
        assertThat(html).doesNotContain("tabindex=\"1\"").doesNotContain("tabindex=\"2\"");
    }

    /** [US-028-AC6] Status and errors are announced to screen readers; the list has an accessible name. */
    @Test
    void us028Ac6_statusAndErrorsAreAnnounced() throws IOException {
        String html = resource("static/notification-history.html");
        String js = resource("static/js/notification-history.js");

        assertThat(html).contains("role=\"status\" aria-live=\"polite\"")
                .contains("role=\"alert\"")
                .contains("aria-label=\"Notifications, most recent first\"");
        assertThat(js).contains("document.documentElement.lang")
                .contains("aria-busy")
                .contains("setAttribute(\"aria-hidden\", \"true\")")
                .contains("time.dateTime")
                .doesNotContain("innerHTML")
                .doesNotContain("localStorage")
                .doesNotContain("sessionStorage");
    }

    /** [US-028-AC6] Type is conveyed by text (category label + type heading), not by colour alone. */
    @Test
    void us028Ac6_typeIsNotConveyedByColourAlone() throws IOException {
        String js = resource("static/js/notification-history.js");

        assertThat(js).contains("category.textContent = n.categoryLabel")
                .contains("title.textContent = n.typeLabel");
    }

    /** [US-028-AC6] Text/background colour pairs meet the WCAG AA 4.5:1 contrast ratio. */
    @Test
    void us028Ac6_colourPairsMeetContrastRatio() throws IOException {
        Map<String, Map<String, String>> pairs = new HashMap<>();
        Matcher matcher = COLOR_VAR.matcher(resource("static/css/notification-history.css"));
        while (matcher.find()) {
            pairs.computeIfAbsent(matcher.group(1), k -> new HashMap<>()).put(matcher.group(2), matcher.group(3));
        }
        assertThat(pairs).containsKeys("text", "muted", "transaction", "balance", "bill", "error", "button", "link");
        pairs.forEach((name, pair) -> assertThat(contrast(pair.get("fg"), pair.get("bg")))
                .as("contrast of " + name).isGreaterThanOrEqualTo(4.5));
    }

    /** [US-028-AC6] Sizing is relative (rem/em) so 200% text size reflows instead of clipping content. */
    @ParameterizedTest
    @ValueSource(strings = {"height:\\s*\\d+px", "font-size:\\s*\\d+px", "overflow:\\s*hidden",
            "text-overflow", "white-space:\\s*nowrap"})
    void us028Ac6_layoutSurvivesTextResize(String forbidden) throws IOException {
        String css = resource("static/css/notification-history.css");

        assertThat(Pattern.compile(forbidden).matcher(css).find()).as(forbidden).isFalse();
        assertThat(css).contains("flex-wrap: wrap").contains(":focus-visible");
    }

    private static double contrast(String fg, String bg) {
        double a = luminance(fg);
        double b = luminance(bg);
        return (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05);
    }

    private static double luminance(String hex) {
        double[] rgb = new double[3];
        for (int i = 0; i < 3; i++) {
            double c = Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16) / 255.0;
            rgb[i] = c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
        }
        return 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2];
    }
}
