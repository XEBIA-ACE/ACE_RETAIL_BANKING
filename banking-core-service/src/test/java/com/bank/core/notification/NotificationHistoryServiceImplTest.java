package com.bank.core.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bank.core.notification.config.NotificationHistoryProperties;
import com.bank.core.notification.domain.NotificationCategory;
import com.bank.core.notification.domain.NotificationHistoryEntryPoint;
import com.bank.core.notification.domain.NotificationLog;
import com.bank.core.notification.domain.NotificationType;
import com.bank.core.notification.dto.NotificationHistoryItemDto;
import com.bank.core.notification.dto.NotificationHistoryQuery;
import com.bank.core.notification.dto.NotificationHistoryResponse;
import com.bank.core.notification.repository.NotificationHistoryRepository;
import com.bank.core.notification.service.NotificationHistoryLocalizer;
import com.bank.core.notification.service.NotificationHistoryServiceImpl;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.support.ResourceBundleMessageSource;

class NotificationHistoryServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-10-23T12:05:00Z");
    private static final ZoneId ZURICH = ZoneId.of("Europe/Zurich");

    private final NotificationHistoryRepository repository = mock(NotificationHistoryRepository.class);
    private NotificationHistoryLocalizer localizer;
    private NotificationHistoryServiceImpl service;

    static NotificationHistoryLocalizer localizer() {
        ResourceBundleMessageSource messages = new ResourceBundleMessageSource();
        messages.setBasename("messages/notification-history");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);
        return new NotificationHistoryLocalizer(messages);
    }

    @BeforeEach
    void setUp() {
        localizer = localizer();
        service = serviceWithRetention(Duration.ofDays(90));
    }

    private NotificationHistoryServiceImpl serviceWithRetention(Duration retention) {
        return new NotificationHistoryServiceImpl(repository, localizer,
                new NotificationHistoryProperties(retention, ZURICH), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static NotificationLog log(NotificationType type, String description, Instant at) {
        return NotificationLog.builder().customerId("cust-a").notificationType(type)
                .description(description).createdTimestamp(at).locale("en").build();
    }

    private static NotificationHistoryQuery query(Locale locale, ZoneId zone, Set<NotificationType> types) {
        return new NotificationHistoryQuery("cust-a", types, locale, zone, NotificationHistoryEntryPoint.DASHBOARD);
    }

    /** [US-028-AC1] Each entry carries type, high-level description and a localized timestamp. */
    @Test
    void us028Ac1_entriesExposeTypeDescriptionAndLocalizedTimestamp() {
        when(repository.findCustomerHistory(eq("cust-a"), any(), any())).thenReturn(List.of(
                log(NotificationType.LARGE_TRANSACTION, "Card payment over your limit", NOW)));

        NotificationHistoryResponse response = service.getHistory(query(Locale.ENGLISH, null, Set.of()));

        NotificationHistoryItemDto item = response.notifications().get(0);
        assertThat(item.notificationType()).isEqualTo(NotificationType.LARGE_TRANSACTION);
        assertThat(item.typeLabel()).isEqualTo("Large transaction");
        assertThat(item.description()).isEqualTo("Card payment over your limit");
        assertThat(item.timestamp()).isEqualTo(NOW);
        assertThat(item.formattedTimestamp()).contains("Oct 23, 2026").contains("2:05");
        assertThat(response.entryPoint()).isEqualTo("DASHBOARD");
        assertThat(response.returnLabel()).isEqualTo("Back to dashboard");
    }

    /** [US-028-AC2] Each type maps to a distinct category so transaction/balance/bill alerts are distinguishable. */
    @Test
    void us028Ac2_typesMapToDistinctCategoriesAndLabels() {
        when(repository.findCustomerHistory(eq("cust-a"), any(), any())).thenReturn(List.of(
                log(NotificationType.LARGE_TRANSACTION, "a", NOW),
                log(NotificationType.LOW_BALANCE, "b", NOW),
                log(NotificationType.BILL_REMINDER, "c", NOW)));

        List<NotificationHistoryItemDto> items =
                service.getHistory(query(Locale.ENGLISH, ZURICH, Set.of())).notifications();

        assertThat(items).extracting(NotificationHistoryItemDto::category).containsExactly(
                NotificationCategory.TRANSACTION, NotificationCategory.BALANCE, NotificationCategory.BILL);
        assertThat(items).extracting(NotificationHistoryItemDto::categoryLabel)
                .containsExactly("Transaction alert", "Balance alert", "Bill alert");
    }

    /** [US-028-AC2] Filtering by type passes only the selected types to the repository; none selected = all. */
    @Test
    @SuppressWarnings("unchecked")
    void us028Ac2_typeFilterIsAppliedAndDefaultsToAllTypes() {
        ArgumentCaptor<Collection<NotificationType>> types = ArgumentCaptor.forClass(Collection.class);

        service.getHistory(query(Locale.ENGLISH, ZURICH, Set.of(NotificationType.LOW_BALANCE)));
        service.getHistory(query(Locale.ENGLISH, ZURICH, Set.of()));

        verify(repository, org.mockito.Mockito.times(2)).findCustomerHistory(eq("cust-a"), any(), types.capture());
        assertThat(types.getAllValues().get(0)).containsExactly(NotificationType.LOW_BALANCE);
        assertThat(types.getAllValues().get(1)).containsExactlyInAnyOrderElementsOf(
                EnumSet.allOf(NotificationType.class));
    }

    /** [US-028-AC3] Reads are bounded by the configured retention window (baseline 90 days). */
    @Test
    void us028Ac3_queryUsesConfiguredRetentionCutoff() {
        ArgumentCaptor<Instant> cutoff = ArgumentCaptor.forClass(Instant.class);

        NotificationHistoryResponse response = service.getHistory(query(Locale.ENGLISH, ZURICH, Set.of()));

        verify(repository).findCustomerHistory(eq("cust-a"), cutoff.capture(), any());
        assertThat(cutoff.getValue()).isEqualTo(NOW.minus(Duration.ofDays(90)));
        assertThat(response.retentionDays()).isEqualTo(90);
    }

    /** [US-028-AC3] The retention window is configurable and the purge job uses the very same cut-off. */
    @Test
    void us028Ac3_purgeUsesSameCutoffAsReads() {
        NotificationHistoryServiceImpl thirtyDays = serviceWithRetention(Duration.ofDays(30));
        when(repository.deleteOlderThan(NOW.minus(Duration.ofDays(30)))).thenReturn(4);

        assertThat(thirtyDays.retentionCutoff()).isEqualTo(NOW.minus(Duration.ofDays(30)));
        assertThat(thirtyDays.purgeExpired()).isEqualTo(4);
        assertThat(new NotificationHistoryProperties(null, null).retention()).isEqualTo(Duration.ofDays(90));
    }

    /** [US-028-AC4] Queries are always scoped to the customer id taken from the authenticated principal. */
    @Test
    void us028Ac4_historyIsScopedToQueryCustomer() {
        service.getHistory(new NotificationHistoryQuery("cust-b", Set.of(), Locale.ENGLISH, ZURICH, null));

        verify(repository).findCustomerHistory(eq("cust-b"), any(), any());
        org.assertj.core.api.Assertions.assertThatNullPointerException().isThrownBy(() ->
                service.getHistory(new NotificationHistoryQuery(null, Set.of(), Locale.ENGLISH, ZURICH, null)));
    }

    /** [US-028-AC5] Type labels and timestamps are localized for de, fr, it and en, in the requested zone. */
    @Test
    void us028Ac5_labelsAndTimestampsAreLocalized() {
        when(repository.findCustomerHistory(eq("cust-a"), any(), any())).thenReturn(List.of(
                log(NotificationType.BILL_REMINDER, "Bill due", NOW)));

        assertThat(firstItem(Locale.GERMAN, ZURICH).typeLabel()).isEqualTo("Rechnungserinnerung");
        assertThat(firstItem(Locale.GERMAN, ZURICH).formattedTimestamp()).startsWith("23.10.2026").contains("14:05");
        assertThat(firstItem(Locale.FRENCH, ZURICH).typeLabel()).isEqualTo("Rappel de facture");
        assertThat(firstItem(Locale.FRENCH, ZURICH).formattedTimestamp()).contains("23 oct. 2026").contains("14:05");
        assertThat(firstItem(Locale.ITALIAN, ZURICH).typeLabel()).isEqualTo("Promemoria fattura");
        assertThat(firstItem(Locale.ITALIAN, ZURICH).formattedTimestamp()).contains("23 ott 2026").contains("14:05");
        assertThat(firstItem(Locale.ENGLISH, ZoneId.of("America/New_York")).formattedTimestamp())
                .contains("Oct 23, 2026").contains("8:05");
    }

    /** [US-028-AC5] Response reports the effective locale and time zone; default zone applies when none given. */
    @Test
    void us028Ac5_responseReportsLocaleAndTimeZone() {
        NotificationHistoryResponse response = service.getHistory(query(Locale.FRENCH, null, Set.of()));

        assertThat(response.locale()).isEqualTo("fr");
        assertThat(response.timeZone()).startsWith("Europe/Zurich");
        assertThat(service.getLabels(Locale.ITALIAN).labels())
                .containsEntry("history.title", "Cronologia delle notifiche");
    }

    private NotificationHistoryItemDto firstItem(Locale locale, ZoneId zone) {
        return service.getHistory(query(locale, zone, Set.of())).notifications().get(0);
    }
}
