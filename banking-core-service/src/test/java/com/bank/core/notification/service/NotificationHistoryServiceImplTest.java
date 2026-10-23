package com.bank.core.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bank.core.notification.domain.DeliveryStatus;
import com.bank.core.notification.domain.NotificationHistoryRecord;
import com.bank.core.notification.dto.NotificationDeliveredRequest;
import com.bank.core.notification.dto.NotificationHistoryResponse;
import com.bank.core.notification.dto.RecordNotificationResult;
import com.bank.core.notification.exception.NotificationHistoryConflictException;
import com.bank.core.notification.mapper.NotificationHistoryMapperImpl;
import com.bank.core.notification.repository.NotificationHistoryRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class NotificationHistoryServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-10-20T10:00:00Z");

    @Mock
    private NotificationHistoryRepository repository;
    private NotificationHistoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationHistoryServiceImpl(repository, new NotificationHistoryMapperImpl());
    }

    /** [US-009-ac-1] FR-001: history is read customer-scoped and returned in repository (newest-first) order. */
    @Test
    void us009Ac1_returnsCustomerHistoryMostRecentFirst() {
        when(repository.findByCustomerExternalIdOrderByNotificationTimestampDescIdDesc("cust-1")).thenReturn(List.of(
                record("src-2", "cust-1", "LOW_BALANCE", NOW),
                record("src-1", "cust-1", "LARGE_TRANSACTION", NOW.minusSeconds(60))));

        NotificationHistoryResponse response = service.getHistory("cust-1");

        assertThat(response.notifications())
                .extracting("notificationType")
                .containsExactly("LOW_BALANCE", "LARGE_TRANSACTION");
        assertThat(response.notifications()).allSatisfy(item -> assertThat(item.customerId()).isEqualTo("cust-1"));
    }

    /** [US-009-ac-2] FR-002: no history is an empty collection, not an error. */
    @Test
    void us009Ac2_returnsEmptyCollectionWhenNoHistory() {
        when(repository.findByCustomerExternalIdOrderByNotificationTimestampDescIdDesc("cust-1"))
                .thenReturn(List.of());

        assertThat(service.getHistory("cust-1").notifications()).isEmpty();
    }

    /** [US-009-ac-3] FR-003: a completed delivery is persisted to the customer's history. */
    @Test
    void us009Ac3_persistsNewNotificationOnDeliveryCompletion() {
        when(repository.findBySourceNotificationId("src-1")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RecordNotificationResult result = service.recordDelivered("src-1", request("cust-1"));

        ArgumentCaptor<NotificationHistoryRecord> saved = ArgumentCaptor.forClass(NotificationHistoryRecord.class);
        verify(repository).saveAndFlush(saved.capture());
        assertThat(result.created()).isTrue();
        assertThat(saved.getValue().getCustomerExternalId()).isEqualTo("cust-1");
        assertThat(saved.getValue().getSourceNotificationId()).isEqualTo("src-1");
        assertThat(saved.getValue().getExternalId()).isNotBlank();
        assertThat(saved.getValue().getRetentionExpiresAt()).isNull();
    }

    /** [US-009-ac-3] T002: replaying the same delivery completion is idempotent. */
    @Test
    void us009Ac3_replayReturnsExistingRecordWithoutWriting() {
        when(repository.findBySourceNotificationId("src-1"))
                .thenReturn(Optional.of(record("src-1", "cust-1", "BILL_REMINDER", NOW)));

        RecordNotificationResult result = service.recordDelivered("src-1", request("cust-1"));

        assertThat(result.created()).isFalse();
        verify(repository, never()).saveAndFlush(any());
    }

    /** [US-009-ac-3] T002: a concurrent duplicate insert resolves to the record that won the race. */
    @Test
    void us009Ac3_concurrentDuplicateResolvesToExistingRecord() {
        when(repository.findBySourceNotificationId("src-1"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(record("src-1", "cust-1", "BILL_REMINDER", NOW)));
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThat(service.recordDelivered("src-1", request("cust-1")).created()).isFalse();
    }

    @Test
    void us009Ac3_rejectsReuseOfIdentifierForDifferentCustomer() {
        when(repository.findBySourceNotificationId("src-1"))
                .thenReturn(Optional.of(record("src-1", "cust-1", "BILL_REMINDER", NOW)));

        assertThatThrownBy(() -> service.recordDelivered("src-1", request("cust-2")))
                .isInstanceOf(NotificationHistoryConflictException.class);
    }

    @Test
    void us009Ac3_propagatesIntegrityViolationWhenNoRecordExists() {
        when(repository.findBySourceNotificationId("src-1")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unknown customer"));

        assertThatThrownBy(() -> service.recordDelivered("src-1", request("cust-1")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static NotificationDeliveredRequest request(String customerId) {
        return new NotificationDeliveredRequest(customerId, "BILL_REMINDER", "Electricity bill due tomorrow",
                NOW, DeliveryStatus.DELIVERED, NOW.plusSeconds(1));
    }

    private static NotificationHistoryRecord record(String sourceId, String customerId, String type, Instant at) {
        return NotificationHistoryRecord.builder()
                .externalId("ext-" + sourceId)
                .sourceNotificationId(sourceId)
                .customerExternalId(customerId)
                .notificationType(type)
                .message("message " + sourceId)
                .notificationTimestamp(at)
                .deliveryStatus(DeliveryStatus.DELIVERED)
                .deliveredAt(at.plusSeconds(1))
                .build();
    }
}
