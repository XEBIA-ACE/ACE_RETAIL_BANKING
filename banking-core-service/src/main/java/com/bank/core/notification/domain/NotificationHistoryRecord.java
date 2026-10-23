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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** US-009 T001: a smart notification persisted to a customer's in-app notification history. */
@Entity
@Table(name = "notification_history")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationHistoryRecord extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, length = 36, updatable = false)
    private String externalId;

    @Column(name = "source_notification_id", nullable = false, unique = true, length = 100, updatable = false)
    private String sourceNotificationId;

    @Column(name = "customer_external_id", nullable = false, length = 36, updatable = false)
    private String customerExternalId;

    @Column(name = "notification_type", nullable = false, length = 40)
    private String notificationType;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "notification_timestamp", nullable = false)
    private Instant notificationTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 20)
    private DeliveryStatus deliveryStatus;

    @Column(name = "delivered_at", nullable = false)
    private Instant deliveredAt;

    /** Retention expiry marker; left null until retention rules are defined (open gap, T005). */
    @Column(name = "retention_expires_at")
    private Instant retentionExpiresAt;
}
