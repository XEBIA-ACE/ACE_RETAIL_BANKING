package com.bank.core.notification.service;

import com.bank.core.notification.repository.NotificationHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Purges notification logs that fall outside the retention policy (FR-007). */
@Component
public class NotificationRetentionJob {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRetentionJob.class);

    private final NotificationHistoryRepository repository;
    private final NotificationRetentionPolicy retentionPolicy;

    public NotificationRetentionJob(NotificationHistoryRepository repository,
                                    NotificationRetentionPolicy retentionPolicy) {
        this.repository = repository;
        this.retentionPolicy = retentionPolicy;
    }

    @Scheduled(cron = "${notification.history.purge-cron:0 30 2 * * *}")
    @Transactional
    public int purgeExpired() {
        int deleted = repository.deleteOlderThan(retentionPolicy.cutoff());
        LOG.info("Purged {} notification log entries older than {} days", deleted, retentionPolicy.retentionDays());
        return deleted;
    }
}
