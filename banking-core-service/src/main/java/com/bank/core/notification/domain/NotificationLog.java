package com.bank.core.notification.domain;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Append-only record of a customer notification that was sent (DS-001).
 * {@code description} must be a concise, high-level text without sensitive data (e.g. masked account numbers).
 */
@Entity
@Table(name = "notification_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "external_id", nullable = false, unique = true, length = 36, updatable = false)
    private String externalId;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 32, updatable = false)
    private NotificationType notificationType;

    @Column(name = "description", nullable = false, length = 255, updatable = false)
    private String description;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private Instant createdTimestamp;

    @Column(name = "locale", nullable = false, length = 10, updatable = false)
    private String locale;

    public NotificationLog(Long customerId, NotificationType notificationType, String description,
                           Instant createdTimestamp, String locale) {
        this.externalId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.notificationType = notificationType;
        this.description = description;
        this.createdTimestamp = createdTimestamp;
        this.locale = locale;
    }
}
