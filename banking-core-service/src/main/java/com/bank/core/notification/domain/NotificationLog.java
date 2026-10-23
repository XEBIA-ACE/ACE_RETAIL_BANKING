package com.bank.core.notification.domain;

import com.bank.core.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** A notification sent to a customer, as recorded for the in-app history (DS-001). */
@Getter
@Entity
@Table(name = "notification_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationLog extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false, unique = true, updatable = false, length = 36)
    private String notificationId;

    @Column(name = "customer_id", nullable = false, updatable = false, length = 36)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 40)
    private NotificationType notificationType;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private Instant createdTimestamp;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Builder
    private NotificationLog(String customerId, NotificationType notificationType, String description,
            Instant createdTimestamp, String locale) {
        this.notificationId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.notificationType = notificationType;
        this.description = description;
        this.createdTimestamp = createdTimestamp;
        this.locale = locale;
    }
}
