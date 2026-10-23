package com.bank.core.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.core.notification.service.NotificationHistoryService;
import com.bank.core.support.TestJwtFactory;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** End-to-end characterization tests for US-028 (H2 in MySQL mode, Flyway migrations applied). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class NotificationHistoryIT {

    private static final String HISTORY = "/api/v1/notifications/history";
    private static final ParameterizedTypeReference<Map<String, Object>> JSON = new ParameterizedTypeReference<>() { };

    @Autowired
    private TestRestTemplate rest;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private NotificationHistoryService service;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM notification_log");
        jdbc.update("DELETE FROM customers");
        insertCustomer("cust-a");
        insertCustomer("cust-b");
    }

    /**
     * [US-028-AC1] Obligation ad7cb7df-59fa-59f0-96ff-d061d6d89412:test:characterization:US-028-ac-1 (T016).
     * A signed-in customer opening the history from any entry point sees their notifications, newest first,
     * each with type, description and localized timestamp.
     */
    @ParameterizedTest
    @CsvSource({"dashboard,/dashboard", "security-center,/security-center", "bill-payments,/bill-payments"})
    void us028Ac1_customerSeesOwnHistoryFromEveryEntryPoint(String from, String returnPath) {
        insertLog("cust-a", "LOW_BALANCE", "Balance below CHF 100", daysAgo(5));
        insertLog("cust-a", "LARGE_TRANSACTION", "Payment of CHF 2,500 sent", daysAgo(1));

        Map<String, Object> body = history("cust-a", "?from=" + from + "&lang=en&tz=UTC").getBody();

        assertThat(body).containsEntry("returnPath", returnPath).containsEntry("retentionDays", 90);
        List<Map<String, Object>> items = notifications(body);
        assertThat(items).extracting(i -> i.get("notificationType")).containsExactly("LARGE_TRANSACTION", "LOW_BALANCE");
        assertThat(items.get(0))
                .containsEntry("typeLabel", "Large transaction")
                .containsEntry("description", "Payment of CHF 2,500 sent")
                .containsKeys("notificationId", "timestamp", "formattedTimestamp")
                .doesNotContainKey("customerId");
        assertThat((String) items.get(0).get("formattedTimestamp")).contains("UTC");
    }

    /**
     * [US-028-AC2] Obligation ad7cb7df-59fa-59f0-96ff-d061d6d89412:test:characterization:US-028-ac-2 (T017).
     * Entries indicate their type and whether they relate to transactions, balances or bills; filterable by type.
     */
    @Test
    void us028Ac2_entriesIndicateTypeAndCategoryAndCanBeFiltered() {
        insertLog("cust-a", "LARGE_TRANSACTION", "Large payment", daysAgo(3));
        insertLog("cust-a", "LOW_BALANCE", "Low balance", daysAgo(2));
        insertLog("cust-a", "BILL_REMINDER", "Electricity bill due", daysAgo(1));

        List<Map<String, Object>> all = notifications(history("cust-a", "").getBody());
        assertThat(all).extracting(i -> i.get("category")).containsExactly("BILL", "BALANCE", "TRANSACTION");
        assertThat(all).extracting(i -> i.get("categoryLabel"))
                .containsExactly("Bill alert", "Balance alert", "Transaction alert");

        List<Map<String, Object>> bills = notifications(history("cust-a", "?types=BILL_REMINDER").getBody());
        assertThat(bills).extracting(i -> i.get("notificationType")).containsExactly("BILL_REMINDER");
    }

    /**
     * [US-028-AC3] Obligation ad7cb7df-59fa-59f0-96ff-d061d6d89412:test:characterization:US-028-ac-3 (T018).
     * Notifications older than the retention window are not shown, and the purge removes the same records.
     */
    @Test
    void us028Ac3_notificationsOutsideRetentionWindowAreHiddenAndPurged() {
        insertLog("cust-a", "LOW_BALANCE", "Inside window", daysAgo(89));
        insertLog("cust-a", "LOW_BALANCE", "Outside window", daysAgo(91));

        assertThat(notifications(history("cust-a", "").getBody()))
                .extracting(i -> i.get("description")).containsExactly("Inside window");

        assertThat(service.purgeExpired()).isEqualTo(1);
        assertThat(jdbc.queryForList("SELECT description FROM notification_log", String.class))
                .containsExactly("Inside window");
    }

    /**
     * [US-028-AC4] Obligation ad7cb7df-59fa-59f0-96ff-d061d6d89412:test:characterization:US-028-ac-4 (T019).
     * Only the authenticated customer's notifications are visible; no parameter exposes another customer's.
     */
    @Test
    void us028Ac4_onlyAuthenticatedCustomersNotificationsAreVisible() {
        insertLog("cust-a", "LOW_BALANCE", "A's alert", daysAgo(1));
        insertLog("cust-b", "BILL_REMINDER", "B's alert", daysAgo(1));

        assertThat(notifications(history("cust-a", "?customerId=cust-b").getBody()))
                .extracting(i -> i.get("description")).containsExactly("A's alert");
        assertThat(notifications(history("cust-b", "").getBody()))
                .extracting(i -> i.get("description")).containsExactly("B's alert");
        assertThat(rest.getForEntity(HISTORY, String.class).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * [US-028-AC5] Obligation ad7cb7df-59fa-59f0-96ff-d061d6d89412:test:characterization:US-028-ac-5 (T020).
     * Labels and system messages follow the chosen language; timestamps follow locale and time zone.
     */
    @ParameterizedTest
    @CsvSource({
            "de,Benachrichtigungsverlauf,Niedriger Kontostand",
            "fr,Historique des notifications,Solde bas",
            "it,Cronologia delle notifiche,Saldo basso",
            "en,Notification history,Low balance"})
    void us028Ac5_screenIsLocalizedForSupportedLanguages(String lang, String title, String typeLabel) {
        Instant sentAt = daysAgo(1);
        insertLog("cust-a", "LOW_BALANCE", "Balance alert", sentAt);

        Map<String, Object> labels = rest.exchange(HISTORY + "/labels?lang=" + lang, HttpMethod.GET, null, JSON)
                .getBody();
        assertThat(labels).containsEntry("locale", lang);
        @SuppressWarnings("unchecked")
        Map<String, String> screen = (Map<String, String>) labels.get("labels");
        assertThat(screen).containsEntry("history.title", title);

        ResponseEntity<Map<String, Object>> response = history("cust-a", "?lang=" + lang + "&tz=Europe/Zurich");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_LANGUAGE)).isEqualTo(lang);
        Map<String, Object> item = notifications(response.getBody()).get(0);
        String localizedDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(Locale.forLanguageTag(lang)).withZone(ZoneId.of("Europe/Zurich")).format(sentAt);
        assertThat(item).containsEntry("typeLabel", typeLabel);
        assertThat((String) item.get("formattedTimestamp")).startsWith(localizedDate);
    }

    /** [US-028-AC6] (T021) The screen, script and stylesheet are served without authentication. */
    @Test
    void us028Ac6_screenAssetsAreServed() {
        assertThat(rest.getForEntity("/notification-history.html", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rest.getForEntity("/js/notification-history.js", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rest.getForEntity("/css/notification-history.css", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> history(String customerId, String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(TestJwtFactory.tokenForCustomer(customerId));
        ResponseEntity<Map<String, Object>> response =
                rest.exchange(HISTORY + query, HttpMethod.GET, new HttpEntity<>(headers), JSON);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> notifications(Map<String, Object> body) {
        return (List<Map<String, Object>>) body.get("notifications");
    }

    private static Instant daysAgo(int days) {
        return Instant.now().minus(Duration.ofDays(days));
    }

    private void insertCustomer(String externalId) {
        jdbc.update("INSERT INTO customers (external_id, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                externalId, "Test", externalId, externalId + "@example.com");
    }

    private void insertLog(String customerId, String type, String description, Instant at) {
        Timestamp now = Timestamp.from(Instant.now());
        jdbc.update("""
                INSERT INTO notification_log (notification_id, customer_id, notification_type, description,
                    created_timestamp, locale, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, 'en', ?, ?)
                """, UUID.randomUUID().toString(), customerId, type, description, Timestamp.from(at), now, now);
    }
}
