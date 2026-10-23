package com.bank.core.notification.repository;

import com.bank.core.notification.domain.NotificationLog;
import com.bank.core.notification.domain.NotificationType;
import java.time.Instant;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Customer-scoped access to notification logs (DS-002). Every read requires the authenticated
 * customer's id and a retention cutoff, so no query can return another customer's or expired records.
 */
public interface NotificationHistoryRepository extends JpaRepository<NotificationLog, Long> {

    @Query("""
            SELECT n FROM NotificationLog n
            WHERE n.customerId = :customerId
              AND n.notificationType IN :types
              AND n.createdTimestamp >= :cutoff
            ORDER BY n.createdTimestamp DESC, n.notificationId DESC
            """)
    Page<NotificationLog> findCustomerHistory(@Param("customerId") Long customerId,
                                              @Param("types") Collection<NotificationType> types,
                                              @Param("cutoff") Instant cutoff,
                                              Pageable pageable);

    @Modifying
    @Query("DELETE FROM NotificationLog n WHERE n.createdTimestamp < :cutoff")
    int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
