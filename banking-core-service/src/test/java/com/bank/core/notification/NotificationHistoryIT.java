package com.bank.core.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.bank.core.support.TestJwtFactory;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * End-to-end characterization tests for US-009 (H2 in MySQL mode, Flyway migrations applied).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class NotificationHistoryIT {

    private static final String HISTORY = "/api/v1/notifications/history";
    private static final String NFR_MAX_LOAD_MS = "us009.nfr.max-load-ms";
    private static final ParameterizedTypeReference<Map<String, List<Map<String, Object>>>> HISTORY_TYPE =
            new ParameterizedTypeReference<>() { };

    @Autowired
    private TestRestTemplate rest;
    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM notification_history");
        jdbc.update("DELETE FROM customers");
        insertCustomer("cust-a");
        insertCustomer("cust-b");
    }

    /**
     * [US-009-ac-1] Obligation 8b9cb496-7190-514c-af95-dac72a1437f9:test:characterization:US-009-ac-1
     * Authenticated customer with history sees only their notifications, most recent first.
     */
    @Test
    void us009Ac1_authenticatedCustomerSeesOwnHistoryInReverseChronologicalOrder() {
        deliver("src-1", "cust-a", "LARGE_TRANSACTION", "2026-10-01T09:00:00Z");
        deliver("src-3", "cust-a", "BILL_REMINDER", "2026-10-03T09:00:00Z");
        deliver("src-2", "cust-a", "LOW_BALANCE", "2026-10-02T09:00:00Z");
        deliver("src-other", "cust-b", "LOW_BALANCE", "2026-10-04T09:00:00Z");

        ResponseEntity<Map<String, List<Map<String, Object>>>> response = history("cust-a");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> notifications = response.getBody().get("notifications");
        assertThat(notifications).extracting(item -> item.get("notificationType"))
                .containsExactly("BILL_REMINDER", "LOW_BALANCE", "LARGE_TRANSACTION");
        assertThat(notifications).allSatisfy(item -> assertThat(item)
                .containsEntry("customerId", "cust-a")
                .containsKeys("notificationId", "message", "notificationTimestamp", "deliveryStatus",
                        "deliveredAt", "retentionExpiresAt"));
    }

    /** [US-009-ac-1] FR-001: authentication is enforced at the API layer. */
    @Test
    void us009Ac1_unauthenticatedRequestIsRejected() {
        assertThat(rest.getForEntity(HISTORY, String.class).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(rest.exchange(HISTORY, HttpMethod.GET, bearer(TestJwtFactory.tokenWithoutCustomer(), null),
                String.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    /**
     * [US-009-ac-2] Obligation 8b9cb496-7190-514c-af95-dac72a1437f9:test:characterization:US-009-ac-2
     * Customer with no history receives an empty collection (200), not an error.
     */
    @Test
    void us009Ac2_customerWithoutHistoryReceivesEmptyCollection() {
        deliver("src-other", "cust-b", "LOW_BALANCE", "2026-10-04T09:00:00Z");

        ResponseEntity<Map<String, List<Map<String, Object>>>> response = history("cust-a");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody()).containsOnlyKeys("notifications");
        assertThat(response.getBody().get("notifications")).isEmpty();
    }

    /** [US-009-ac-2] FR-002: the screen ships a neutral empty-state, separate from the error banner. */
    @Test
    void us009Ac2_screenProvidesNonErrorEmptyState() {
        ResponseEntity<String> page = rest.getForEntity("/notification-history.html", String.class);

        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page.getBody())
                .contains("id=\"empty-state\" class=\"status empty\" role=\"status\"")
                .contains("No notification history is available yet.")
                .contains("id=\"error-banner\" class=\"banner\" role=\"alert\" hidden");
    }

    /**
     * [US-009-ac-3] Obligation 8b9cb496-7190-514c-af95-dac72a1437f9:test:characterization:US-009-ac-3
     * Delivery completion persists the notification; it then appears in the customer's history.
     * Replaying the same completion is idempotent.
     */
    @Test
    void us009Ac3_deliveredNotificationAppearsInHistoryIdempotently() {
        ResponseEntity<Map<String, Object>> first = deliver("src-1", "cust-a", "LOW_BALANCE", "2026-10-01T09:00:00Z");
        ResponseEntity<Map<String, Object>> replay = deliver("src-1", "cust-a", "LOW_BALANCE", "2026-10-01T09:00:00Z");

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(replay.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(replay.getBody().get("notificationId")).isEqualTo(first.getBody().get("notificationId"));
        List<Map<String, Object>> notifications = history("cust-a").getBody().get("notifications");
        assertThat(notifications).singleElement()
                .satisfies(item -> assertThat(item)
                        .containsEntry("notificationId", first.getBody().get("notificationId"))
                        .containsEntry("deliveryStatus", "DELIVERED"));
    }

    /** [US-009-ac-3] Only the Smart Notifications capability (write scope) may record history. */
    @Test
    void us009Ac3_customerTokenCannotWriteHistory() {
        ResponseEntity<String> response = rest.exchange("/internal/v1/notification-history/src-1", HttpMethod.PUT,
                bearer(TestJwtFactory.tokenForCustomer("cust-a"), body("cust-a", "LOW_BALANCE",
                        "2026-10-01T09:00:00Z")), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM notification_history", Integer.class)).isZero();
    }

    /** [US-009-ac-3] A record for an unknown customer is refused by referential integrity. */
    @Test
    void us009Ac3_unknownCustomerIsRejected() {
        ResponseEntity<Map<String, Object>> response =
                deliver("src-1", "cust-unknown", "LOW_BALANCE", "2026-10-01T09:00:00Z");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    /** [US-009-ac-3] Retention rules part of the obligation; blocked on the open gap. */
    @Test
    @Disabled("Blocked on open gap 'retention rules' (FR-003 / T005): retention duration, maximum record count "
            + "and archival policy are undefined")
    void us009Ac3_historyRespectsDefinedRetentionRules() {
    }

    /**
     * [US-009-ac-4] Obligation 8b9cb496-7190-514c-af95-dac72a1437f9:test:characterization:US-009-ac-4
     * Skipped until the NFR-001..NFR-003 thresholds are supplied (open gap 'NFR thresholds', FR-004 / T009).
     * Run with -Dus009.nfr.max-load-ms=<threshold from NFR-001..NFR-003> to enforce.
     */
    @Test
    void us009Ac4_historyLoadsWithinStandardScreenPerformanceTargets() {
        String threshold = System.getProperty(NFR_MAX_LOAD_MS);
        assumeTrue(threshold != null, "Blocked on open gap 'NFR thresholds' (NFR-001..NFR-003 / T009): "
                + "provide -D" + NFR_MAX_LOAD_MS);
        deliver("src-1", "cust-a", "LARGE_TRANSACTION", "2026-10-01T09:00:00Z");
        deliver("src-2", "cust-a", "LOW_BALANCE", "2026-10-02T09:00:00Z");
        history("cust-a");

        long[] elapsedMs = new long[20];
        for (int i = 0; i < elapsedMs.length; i++) {
            long started = System.nanoTime();
            assertThat(history("cust-a").getStatusCode()).isEqualTo(HttpStatus.OK);
            elapsedMs[i] = (System.nanoTime() - started) / 1_000_000;
        }
        Arrays.sort(elapsedMs);
        assertThat(elapsedMs[18]).isLessThanOrEqualTo(Long.parseLong(threshold));
    }

    private void insertCustomer(String externalId) {
        jdbc.update("INSERT INTO customers (external_id, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                externalId, "Test", "Customer", externalId + "@example.com");
    }

    private ResponseEntity<Map<String, List<Map<String, Object>>>> history(String customerId) {
        return rest.exchange(HISTORY, HttpMethod.GET, bearer(TestJwtFactory.tokenForCustomer(customerId), null),
                HISTORY_TYPE);
    }

    private ResponseEntity<Map<String, Object>> deliver(String sourceId, String customerId, String type,
                                                        String timestamp) {
        return rest.exchange("/internal/v1/notification-history/" + sourceId, HttpMethod.PUT,
                bearer(TestJwtFactory.tokenWithScope("notification-history:write"), body(customerId, type, timestamp)),
                new ParameterizedTypeReference<>() { });
    }

    private static Map<String, Object> body(String customerId, String type, String timestamp) {
        Instant at = Instant.parse(timestamp);
        return Map.of("customerId", customerId, "notificationType", type, "message", type + " alert",
                "notificationTimestamp", at.toString(), "deliveryStatus", "DELIVERED",
                "deliveredAt", at.plusSeconds(2).toString());
    }

    private static <T> HttpEntity<T> bearer(String token, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }
}
