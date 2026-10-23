package com.bank.core.notification.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * US-028 notification history settings. {@code retention} is the single source of truth for the
 * data-retention policy: it drives both the history query window and the purge job (FR-006, FR-007).
 */
@Validated
@ConfigurationProperties(prefix = "notification.history")
public record NotificationHistoryProperties(
        @NotNull @DefaultValue("90d") Duration retention,
        @NotNull @DefaultValue("Europe/Zurich") ZoneId defaultTimeZone,
        @Min(1) @DefaultValue("20") int defaultPageSize,
        @Min(1) @Max(200) @DefaultValue("100") int maxPageSize) {

    public NotificationHistoryProperties {
        if (retention != null && (retention.isNegative() || retention.isZero())) {
            throw new IllegalArgumentException("notification.history.retention must be positive");
        }
    }

    public long retentionDays() {
        return retention.toDays();
    }
}
