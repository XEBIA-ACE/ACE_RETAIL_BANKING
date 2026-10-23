package com.bank.core.notification.service;

import com.bank.core.notification.config.NotificationHistoryProperties;
import com.bank.core.notification.domain.NotificationHistoryEntryPoint;
import com.bank.core.notification.domain.NotificationLog;
import com.bank.core.notification.domain.NotificationType;
import com.bank.core.notification.dto.NotificationHistoryItemDto;
import com.bank.core.notification.dto.NotificationHistoryLabelsResponse;
import com.bank.core.notification.dto.NotificationHistoryLabelsResponse.EntryPointLink;
import com.bank.core.notification.dto.NotificationHistoryQuery;
import com.bank.core.notification.dto.NotificationHistoryResponse;
import com.bank.core.notification.exception.CustomerIdentityMissingException;
import com.bank.core.notification.exception.InvalidNotificationHistoryRequestException;
import com.bank.core.notification.repository.NotificationHistoryRepository;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationHistoryServiceImpl implements NotificationHistoryService {

    private final NotificationHistoryRepository repository;
    private final NotificationRetentionPolicy retentionPolicy;
    private final NotificationHistoryLocalizer localizer;
    private final NotificationHistoryProperties properties;

    public NotificationHistoryServiceImpl(NotificationHistoryRepository repository,
                                          NotificationRetentionPolicy retentionPolicy,
                                          NotificationHistoryLocalizer localizer,
                                          NotificationHistoryProperties properties) {
        this.repository = repository;
        this.retentionPolicy = retentionPolicy;
        this.localizer = localizer;
        this.properties = properties;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationHistoryResponse getHistory(NotificationHistoryQuery query) {
        validate(query);
        List<NotificationType> types = NotificationType.forCategory(query.category());
        Page<NotificationLog> page = repository.findCustomerHistory(query.customerId(), types,
                retentionPolicy.cutoff(), PageRequest.of(query.page(), query.size()));
        List<NotificationHistoryItemDto> items = page.getContent().stream()
                .map(log -> toItem(log, query.locale(), query.zoneId()))
                .toList();
        String summary = localizer.message("status.summary", query.locale(),
                items.size() + (long) query.page() * query.size(), page.getTotalElements());
        return new NotificationHistoryResponse(items, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), retentionPolicy.retentionDays(),
                query.locale().toLanguageTag(), query.zoneId().getId(),
                query.entryPoint() == null ? null : query.entryPoint().getSlug(), summary);
    }

    @Override
    public NotificationHistoryLabelsResponse getLabels(Locale locale) {
        List<EntryPointLink> links = Arrays.stream(NotificationHistoryEntryPoint.values())
                .map(e -> new EntryPointLink(e.getSlug(), e.historyUrl(),
                        localizer.message("open.history", locale)))
                .toList();
        return new NotificationHistoryLabelsResponse(locale.toLanguageTag(),
                localizer.uiLabels(locale, retentionPolicy.retentionDays()), links);
    }

    private void validate(NotificationHistoryQuery query) {
        if (query.customerId() == null) {
            throw new CustomerIdentityMissingException();
        }
        if (query.page() < 0) {
            throw new InvalidNotificationHistoryRequestException("INVALID_PAGE", "error.invalidPage");
        }
        if (query.size() < 1 || query.size() > properties.maxPageSize()) {
            throw new InvalidNotificationHistoryRequestException("INVALID_PAGE_SIZE", "error.invalidPageSize");
        }
    }

    private NotificationHistoryItemDto toItem(NotificationLog log, Locale locale, ZoneId zoneId) {
        NotificationType type = log.getNotificationType();
        return new NotificationHistoryItemDto(
                log.getExternalId(),
                type,
                localizer.typeLabel(type, locale),
                type.getCategory(),
                localizer.categoryLabel(type.getCategory(), locale),
                log.getDescription(),
                log.getCreatedTimestamp().atZone(zoneId).toOffsetDateTime(),
                localizer.formatTimestamp(log.getCreatedTimestamp(), zoneId, locale));
    }
}
