package com.bank.core.notification.service;

import com.bank.core.notification.config.NotificationHistoryProperties;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Single retention cutoff shared by the history query and the purge job, so what customers can see
 * always matches what is retained (FR-006, FR-007).
 */
@Component
public class NotificationRetentionPolicy {

    private final NotificationHistoryProperties properties;
    private final Clock clock;

    public NotificationRetentionPolicy(NotificationHistoryProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public Instant cutoff() {
        return clock.instant().minus(properties.retention());
    }

    public long retentionDays() {
        return properties.retentionDays();
    }
}
