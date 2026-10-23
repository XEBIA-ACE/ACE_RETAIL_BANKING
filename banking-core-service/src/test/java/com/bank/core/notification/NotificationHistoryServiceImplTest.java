package com.bank.core.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
import com.bank.core.notification.exception.CustomerIdentityMissingException;
import com.bank.core.notification.exception.InvalidNotificationHistoryRequestException;
import com.bank.core.notification.repository.NotificationHistoryRepository;
import com.bank.core.notification.service.NotificationHistoryLocalizer;
import com.bank.core.notification.service.NotificationHistoryServiceImpl;
import com.bank.core.notification.service.NotificationRetentionJob;
import com.bank.core.notification.service.NotificationRetentionPolicy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NotificationHistoryServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-10-23T12:00:00Z");
    private static final ZoneId ZURICH = ZoneId.of("Europe/Zurich");

    @Mock
    private NotificationHistoryRepository repository;

    private NotificationHistoryProperties properties;
    private NotificationRetentionPolicy retentionPolicy;
    private NotificationHistoryServiceImpl service;

    static ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages/notification-history");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        source.setAlwaysUseMessageFormat(true);
        return source;
    }

    @BeforeEach
    void setUp() {
        properties = new NotificationHistoryProperties(Duration.ofDays(90), ZURICH, 20, 100);
        retentionPolicy = new NotificationRetentionPolicy(properties, Clock.fixed(NOW, ZoneOffset.UTC));
        service = new NotificationHistoryServiceImpl(repository, retentionPolicy,
                new NotificationHistoryLocalizer(messageSource()), properties);
    }

    private NotificationHistoryQuery query(Long customerId, NotificationCategory category, Locale locale) {
        return new NotificationHistoryQuery(customerId, category, NotificationHistoryEntryPoint.DASHBOARD,
                locale, ZURICH, 0, 20);
    }

    private void repositoryReturns(NotificationLog... logs) {
        when(repository.findCustomerHistory(anyLong(), anyCollection(), any(Instant.class), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(logs), inv.getArgument(3, Pageable.class), logs.length));
    }

    private static NotificationLog log(NotificationType type, String description) {
        return new NotificationLog(1L, type, description, NOW.minus(Duration.ofDays(2)), "en");
    }

    @Test
    void us028Ac1_entriesExposeTypeDescriptionAndLocalizedTimestamp() {
        repositoryReturns(log(NotificationType.LARGE_TRANSACTION, "Card payment of CHF 2,500.00 at Example AG"));

        NotificationHistoryResponse response = service.getHistory(query(1L, null, Locale.ENGLISH));

        NotificationHistoryItemDto item = response.items().get(0);
        assertThat(item.id()).hasSize(36);
        assertThat(item.type()).isEqualTo(NotificationType.LARGE_TRANSACTION);
        assertThat(item.typeLabel()).isEqualTo("Large transaction alert");
        assertThat(item.description()).isEqualTo("Card payment of CHF 2,500.00 at Example AG");
        assertThat(item.occurredAt().toInstant()).isEqualTo(NOW.minus(Duration.ofDays(2)));
        assertThat(item.occurredAt().getOffset()).isEqualTo(ZoneOffset.ofHours(2));
        assertThat(item.occurredAtFormatted()).contains("2026").contains("CEST");
        assertThat(response.entryPoint()).isEqualTo("dashboard");
        assertThat(response.retentionDays()).isEqualTo(90);
    }

    @Test
    void us028Ac2_eachTypeMapsToADistinctCategoryAndLabel() {
        repositoryReturns(log(NotificationType.LARGE_TRANSACTION, "a"), log(NotificationType.LOW_BALANCE, "b"),
                log(NotificationType.BILL_REMINDER, "c"));

        List<NotificationHistoryItemDto> items = service.getHistory(query(1L, null, Locale.ENGLISH)).items();

        assertThat(items).extracting(NotificationHistoryItemDto::category).containsExactly(
                NotificationCategory.TRANSACTION, NotificationCategory.BALANCE, NotificationCategory.BILL);
        assertThat(items).extracting(NotificationHistoryItemDto::categoryLabel)
                .containsExactly("Transaction", "Balance", "Bill");
        assertThat(items).extracting(NotificationHistoryItemDto::typeLabel).doesNotHaveDuplicates();
    }

    @Test
    @SuppressWarnings("unchecked")
    void us028Ac2_categoryFilterRestrictsQueriedTypes() {
        repositoryReturns();

        service.getHistory(query(1L, NotificationCategory.BILL, Locale.ENGLISH));

        ArgumentCaptor<Collection<NotificationType>> types = ArgumentCaptor.forClass(Collection.class);
        verify(repository).findCustomerHistory(eq(1L), types.capture(), any(), any());
        assertThat(types.getValue()).containsExactly(NotificationType.BILL_REMINDER);
    }

    @Test
    void us028Ac3_queryUsesConfiguredRetentionCutoff() {
        repositoryReturns();

        service.getHistory(query(1L, null, Locale.ENGLISH));

        verify(repository).findCustomerHistory(eq(1L), anyCollection(),
                eq(NOW.minus(Duration.ofDays(90))), eq(PageRequest.of(0, 20)));
    }

    @Test
    void us028Ac3_purgeJobUsesTheSameRetentionCutoff() {
        when(repository.deleteOlderThan(NOW.minus(Duration.ofDays(90)))).thenReturn(3);

        int deleted = new NotificationRetentionJob(repository, retentionPolicy).purgeExpired();

        assertThat(deleted).isEqualTo(3);
    }

    @Test
    void us028Ac3_retentionIsConfigurable() {
        NotificationHistoryProperties thirtyDays = new NotificationHistoryProperties(Duration.ofDays(30), ZURICH, 20, 100);
        NotificationRetentionPolicy policy = new NotificationRetentionPolicy(thirtyDays, Clock.fixed(NOW, ZoneOffset.UTC));

        assertThat(policy.cutoff()).isEqualTo(NOW.minus(Duration.ofDays(30)));
        assertThatThrownBy(() -> new NotificationHistoryProperties(Duration.ZERO, ZURICH, 20, 100))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void us028Ac4_queryIsScopedToTheGivenCustomer() {
        repositoryReturns();

        service.getHistory(query(42L, null, Locale.ENGLISH));

        verify(repository).findCustomerHistory(eq(42L), anyCollection(), any(), any());
    }

    @Test
    void us028Ac4_missingCustomerIsRejected() {
        assertThatThrownBy(() -> service.getHistory(query(null, null, Locale.ENGLISH)))
                .isInstanceOf(CustomerIdentityMissingException.class);
    }

    @Test
    void us028Ac5_labelsAndTimestampsFollowTheCustomerLocale() {
        repositoryReturns(log(NotificationType.LOW_BALANCE, "Balance below CHF 100.00"));

        NotificationHistoryResponse response = service.getHistory(query(1L, null, Locale.GERMAN));

        NotificationHistoryItemDto item = response.items().get(0);
        assertThat(item.typeLabel()).isEqualTo("Warnung zu niedrigem Kontostand");
        assertThat(item.occurredAtFormatted()).startsWith("21.10.2026").contains("MESZ");
        assertThat(response.summary()).isEqualTo("1 von 1 Benachrichtigungen werden angezeigt.");
        assertThat(response.locale()).isEqualTo("de");
        assertThat(response.timeZone()).isEqualTo("Europe/Zurich");
    }

    @Test
    void invalidPagingIsRejected() {
        NotificationHistoryQuery badSize = new NotificationHistoryQuery(1L, null, null, Locale.ENGLISH, ZURICH, 0, 500);
        NotificationHistoryQuery badPage = new NotificationHistoryQuery(1L, null, null, Locale.ENGLISH, ZURICH, -1, 20);

        assertThatThrownBy(() -> service.getHistory(badSize)).isInstanceOf(InvalidNotificationHistoryRequestException.class);
        assertThatThrownBy(() -> service.getHistory(badPage)).isInstanceOf(InvalidNotificationHistoryRequestException.class);
    }
}
