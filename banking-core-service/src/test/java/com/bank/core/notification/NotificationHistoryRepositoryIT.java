package com.bank.core.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bank.core.notification.domain.NotificationLog;
import com.bank.core.notification.domain.NotificationType;
import com.bank.core.notification.repository.NotificationHistoryRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** Runs the Flyway migrations and the JPQL history query against the database (US-028 DS-001, DS-002). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class NotificationHistoryRepositoryIT {

    private static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.MICROS);
    private static final Instant CUTOFF = NOW.minus(Duration.ofDays(90));
    private static final EnumSet<NotificationType> ALL = EnumSet.allOf(NotificationType.class);

    @Autowired
    private NotificationHistoryRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    private long alice;
    private long bob;

    private long customer(String name) {
        jdbc.update("INSERT INTO customers (external_id, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                java.util.UUID.randomUUID().toString(), name, "Test", name + "-" + System.nanoTime() + "@example.com");
        return jdbc.queryForObject("SELECT MAX(id) FROM customers", Long.class);
    }

    private NotificationLog save(long customerId, NotificationType type, Duration age) {
        return repository.saveAndFlush(new NotificationLog(customerId, type, type + " for " + customerId,
                NOW.minus(age), "en"));
    }

    private List<String> descriptions(long customerId) {
        Page<NotificationLog> page = repository.findCustomerHistory(customerId, ALL, CUTOFF, PageRequest.of(0, 50));
        return page.map(NotificationLog::getDescription).getContent();
    }

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM notification_log");
        alice = customer("alice");
        bob = customer("bob");
    }

    @Test
    void us028Ac1_returnsAllFieldsNewestFirst() {
        save(alice, NotificationType.LOW_BALANCE, Duration.ofDays(3));
        save(alice, NotificationType.LARGE_TRANSACTION, Duration.ofHours(1));

        List<NotificationLog> logs = repository.findCustomerHistory(alice, ALL, CUTOFF, PageRequest.of(0, 10)).getContent();

        assertThat(logs).extracting(NotificationLog::getNotificationType)
                .containsExactly(NotificationType.LARGE_TRANSACTION, NotificationType.LOW_BALANCE);
        NotificationLog first = logs.get(0);
        assertThat(first.getExternalId()).isNotBlank();
        assertThat(first.getCreatedTimestamp()).isEqualTo(NOW.minus(Duration.ofHours(1)));
        assertThat(first.getLocale()).isEqualTo("en");
    }

    @Test
    void us028Ac1_supportsPaging() {
        for (int i = 0; i < 5; i++) {
            save(alice, NotificationType.BILL_REMINDER, Duration.ofDays(i));
        }

        Page<NotificationLog> page = repository.findCustomerHistory(alice, ALL, CUTOFF, PageRequest.of(1, 2));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void us028Ac2_storesAllTypesAndFiltersByType() {
        save(alice, NotificationType.LARGE_TRANSACTION, Duration.ofDays(1));
        save(alice, NotificationType.LOW_BALANCE, Duration.ofDays(1));
        save(alice, NotificationType.BILL_REMINDER, Duration.ofDays(1));

        Page<NotificationLog> bills = repository.findCustomerHistory(alice,
                EnumSet.of(NotificationType.BILL_REMINDER), CUTOFF, PageRequest.of(0, 10));

        assertThat(descriptions(alice)).hasSize(3);
        assertThat(bills.getContent()).extracting(NotificationLog::getNotificationType)
                .containsExactly(NotificationType.BILL_REMINDER);
    }

    @Test
    void us028Ac2_databaseRejectsUnknownTypes() {
        assertThatThrownBy(() -> jdbc.update("INSERT INTO notification_log (external_id, customer_id, notification_type,"
                + " description, created_timestamp, locale) VALUES ('x', ?, 'MARKETING', 'd', CURRENT_TIMESTAMP, 'en')", alice))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void us028Ac3_excludesNotificationsOlderThanRetention() {
        save(alice, NotificationType.LOW_BALANCE, Duration.ofDays(89));
        save(alice, NotificationType.LOW_BALANCE, Duration.ofDays(90));
        save(alice, NotificationType.LOW_BALANCE, Duration.ofDays(91));

        assertThat(descriptions(alice)).hasSize(2);
    }

    @Test
    void us028Ac3_purgeDeletesOnlyExpiredRows() {
        save(alice, NotificationType.BILL_REMINDER, Duration.ofDays(10));
        save(alice, NotificationType.BILL_REMINDER, Duration.ofDays(120));
        save(bob, NotificationType.BILL_REMINDER, Duration.ofDays(200));

        int deleted = repository.deleteOlderThan(CUTOFF);

        assertThat(deleted).isEqualTo(2);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void us028Ac4_neverReturnsAnotherCustomersNotifications() {
        save(alice, NotificationType.LARGE_TRANSACTION, Duration.ofDays(1));
        save(bob, NotificationType.LARGE_TRANSACTION, Duration.ofDays(1));
        save(bob, NotificationType.LOW_BALANCE, Duration.ofDays(2));

        assertThat(descriptions(alice)).containsExactly("LARGE_TRANSACTION for " + alice);
        assertThat(descriptions(bob)).allMatch(d -> d.endsWith(" for " + bob)).hasSize(2);
    }
}
