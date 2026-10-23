package com.bank.core.notification.config;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Notification history settings (FR-006, FR-007). {@code retention} is the single source of truth for the
 * data-retention policy: the history query and the purge job both derive their cut-off from it.
 */
@Validated
@ConfigurationProperties(prefix = "notification.history")
public record NotificationHistoryProperties(@NotNull Duration retention, @NotNull ZoneId defaultTimeZone) {

    public static final Duration BASELINE_RETENTION = Duration.ofDays(90);

    public NotificationHistoryProperties {
        retention = retention == null ? BASELINE_RETENTION : retention;
        defaultTimeZone = defaultTimeZone == null ? ZoneId.of("Europe/Zurich") : defaultTimeZone;
        if (retention.isNegative() || retention.isZero()) {
            throw new IllegalArgumentException("notification.history.retention must be positive");
        }
    }
}
