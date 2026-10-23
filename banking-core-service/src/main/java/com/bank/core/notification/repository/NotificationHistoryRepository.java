package com.bank.core.notification.repository;

import com.bank.core.notification.domain.NotificationLog;
import com.bank.core.notification.domain.NotificationType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Customer-scoped access to notification logs (DS-002). Every read requires the authenticated customer's id
 * and a retention cut-off; there is intentionally no unscoped finder exposed to the history flow.
 */
public interface NotificationHistoryRepository extends JpaRepository<NotificationLog, Long> {

    @Query("""
            SELECT n FROM NotificationLog n
            WHERE n.customerId = :customerId
              AND n.createdTimestamp >= :cutoff
              AND n.notificationType IN :types
            ORDER BY n.createdTimestamp DESC, n.id DESC
            """)
    List<NotificationLog> findCustomerHistory(@Param("customerId") String customerId,
            @Param("cutoff") Instant cutoff, @Param("types") Collection<NotificationType> types);

    @Modifying
    @Query("DELETE FROM NotificationLog n WHERE n.createdTimestamp < :cutoff")
    int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
