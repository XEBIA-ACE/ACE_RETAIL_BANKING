package com.bank.core.notification.repository;

import com.bank.core.notification.domain.NotificationHistoryRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistoryRecord, Long> {

    List<NotificationHistoryRecord> findByCustomerExternalIdOrderByNotificationTimestampDescIdDesc(
            String customerExternalId);

    Optional<NotificationHistoryRecord> findBySourceNotificationId(String sourceNotificationId);
}
