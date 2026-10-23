package com.bank.core.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bank.core.notification.config.NotificationHistoryProperties;
import com.bank.core.notification.service.NotificationHistoryService;
import com.bank.core.notification.service.NotificationRetentionJob;
import java.time.Duration;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

/** [US-028-AC3] Retention window configuration and the scheduled purge that enforces it in storage. */
class NotificationHistoryRetentionConfigTest {

    /** [US-028-AC3] The baseline retention period is 90 days when not configured. */
    @Test
    void us028Ac3_defaultsToBaselineRetention() {
        NotificationHistoryProperties properties = new NotificationHistoryProperties(null, null);

        assertThat(properties.retention()).isEqualTo(Duration.ofDays(90));
        assertThat(properties.defaultTimeZone()).isEqualTo(ZoneId.of("Europe/Zurich"));
    }

    /** [US-028-AC3] The retention period is configurable but must be positive. */
    @Test
    void us028Ac3_retentionIsConfigurableAndMustBePositive() {
        assertThat(new NotificationHistoryProperties(Duration.ofDays(30), ZoneId.of("UTC")).retention())
                .isEqualTo(Duration.ofDays(30));
        assertThatThrownBy(() -> new NotificationHistoryProperties(Duration.ZERO, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NotificationHistoryProperties(Duration.ofDays(-1), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** [US-028-AC3] The scheduled job purges notifications outside the retention window. */
    @Test
    void us028Ac3_retentionJobPurgesExpiredNotifications() {
        NotificationHistoryService service = mock(NotificationHistoryService.class);
        when(service.purgeExpired()).thenReturn(3);

        new NotificationRetentionJob(service).purgeExpired();

        verify(service).purgeExpired();
    }
}
