package com.bank.core.notification.service;

import com.bank.core.notification.domain.NotificationHistoryRecord;
import com.bank.core.notification.dto.NotificationDeliveredRequest;
import com.bank.core.notification.dto.NotificationHistoryResponse;
import com.bank.core.notification.dto.RecordNotificationResult;
import com.bank.core.notification.exception.NotificationHistoryConflictException;
import com.bank.core.notification.mapper.NotificationHistoryMapper;
import com.bank.core.notification.repository.NotificationHistoryRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationHistoryServiceImpl implements NotificationHistoryService {

    private final NotificationHistoryRepository repository;
    private final NotificationHistoryMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public NotificationHistoryResponse getHistory(String customerId) {
        return new NotificationHistoryResponse(
                repository.findByCustomerExternalIdOrderByNotificationTimestampDescIdDesc(customerId)
                        .stream()
                        .map(mapper::toDto)
                        .toList());
    }

    /**
     * Not transactional on purpose: a concurrent duplicate insert must be able to re-read the winner.
     * Retention rules are not applied yet (open gap, T005), so retentionExpiresAt stays null.
     */
    @Override
    public RecordNotificationResult recordDelivered(String sourceNotificationId,
                                                    NotificationDeliveredRequest request) {
        return repository.findBySourceNotificationId(sourceNotificationId)
                .map(existing -> replay(existing, request))
                .orElseGet(() -> insert(sourceNotificationId, request));
    }

    private RecordNotificationResult insert(String sourceNotificationId, NotificationDeliveredRequest request) {
        try {
            NotificationHistoryRecord saved = repository.saveAndFlush(toRecord(sourceNotificationId, request));
            return new RecordNotificationResult(mapper.toDto(saved), true);
        } catch (DataIntegrityViolationException exception) {
            return repository.findBySourceNotificationId(sourceNotificationId)
                    .map(existing -> replay(existing, request))
                    .orElseThrow(() -> exception);
        }
    }

    private RecordNotificationResult replay(NotificationHistoryRecord existing, NotificationDeliveredRequest request) {
        if (!existing.getCustomerExternalId().equals(request.customerId())) {
            throw new NotificationHistoryConflictException(existing.getSourceNotificationId());
        }
        return new RecordNotificationResult(mapper.toDto(existing), false);
    }

    private NotificationHistoryRecord toRecord(String sourceNotificationId, NotificationDeliveredRequest request) {
        return NotificationHistoryRecord.builder()
                .externalId(UUID.randomUUID().toString())
                .sourceNotificationId(sourceNotificationId)
                .customerExternalId(request.customerId())
                .notificationType(request.notificationType())
                .message(request.message())
                .notificationTimestamp(request.notificationTimestamp())
                .deliveryStatus(request.deliveryStatus())
                .deliveredAt(request.deliveredAt())
                .build();
    }
}
