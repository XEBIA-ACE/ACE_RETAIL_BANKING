package com.bank.core.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Enforces the notification data-retention policy in storage, using the same window as reads (FR-007). */
@Component
public class NotificationRetentionJob {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRetentionJob.class);

    private final NotificationHistoryService service;

    public NotificationRetentionJob(NotificationHistoryService service) {
        this.service = service;
    }

    @Scheduled(cron = "${notification.history.purge-cron:0 30 2 * * *}")
    public void purgeExpired() {
        int deleted = service.purgeExpired();
        LOG.info("Notification retention purge removed {} expired notification log(s)", deleted);
    }
}
