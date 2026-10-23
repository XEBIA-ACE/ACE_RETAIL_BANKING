package com.bank.core.notification.service;

import com.bank.core.notification.config.NotificationHistoryProperties;
import com.bank.core.notification.domain.NotificationHistoryEntryPoint;
import com.bank.core.notification.domain.NotificationLog;
import com.bank.core.notification.domain.NotificationType;
import com.bank.core.notification.dto.NotificationHistoryItemDto;
import com.bank.core.notification.dto.NotificationHistoryLabelsResponse;
import com.bank.core.notification.dto.NotificationHistoryQuery;
import com.bank.core.notification.dto.NotificationHistoryResponse;
import com.bank.core.notification.repository.NotificationHistoryRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationHistoryServiceImpl implements NotificationHistoryService {

    private final NotificationHistoryRepository repository;
    private final NotificationHistoryLocalizer localizer;
    private final NotificationHistoryProperties properties;
    private final Clock clock;

    public NotificationHistoryServiceImpl(NotificationHistoryRepository repository,
            NotificationHistoryLocalizer localizer, NotificationHistoryProperties properties, Clock clock) {
        this.repository = repository;
        this.localizer = localizer;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationHistoryResponse getHistory(NotificationHistoryQuery query) {
        Objects.requireNonNull(query.customerId(), "customerId must come from the authenticated principal");
        Locale locale = query.locale();
        ZoneId zone = query.timeZone() == null ? properties.defaultTimeZone() : query.timeZone();
        List<NotificationHistoryItemDto> items = repository
                .findCustomerHistory(query.customerId(), retentionCutoff(), typesOrAll(query))
                .stream()
                .map(log -> toItem(log, locale, zone))
                .toList();
        NotificationHistoryEntryPoint entryPoint = query.entryPoint();
        return new NotificationHistoryResponse(
                locale.toLanguageTag(),
                zone.getId() + " (" + zone.getDisplayName(TextStyle.FULL, locale) + ")",
                properties.retention().toDays(),
                entryPoint == null ? null : entryPoint.name(),
                entryPoint == null ? null : entryPoint.returnPath(),
                entryPoint == null ? null : localizer.returnLabel(entryPoint, locale),
                items);
    }

    @Override
    public NotificationHistoryLabelsResponse getLabels(Locale locale) {
        List<String> supported = NotificationHistoryLocalizer.SUPPORTED_LOCALES.stream()
                .map(Locale::toLanguageTag).toList();
        return new NotificationHistoryLabelsResponse(locale.toLanguageTag(), supported,
                localizer.screenLabels(locale));
    }

    @Override
    public Instant retentionCutoff() {
        return clock.instant().minus(properties.retention());
    }

    @Override
    @Transactional
    public int purgeExpired() {
        return repository.deleteOlderThan(retentionCutoff());
    }

    private static EnumSet<NotificationType> typesOrAll(NotificationHistoryQuery query) {
        return query.types() == null || query.types().isEmpty()
                ? EnumSet.allOf(NotificationType.class)
                : EnumSet.copyOf(query.types());
    }

    private NotificationHistoryItemDto toItem(NotificationLog log, Locale locale, ZoneId zone) {
        NotificationType type = log.getNotificationType();
        return new NotificationHistoryItemDto(
                log.getNotificationId(),
                type,
                type.category(),
                localizer.typeLabel(type, locale),
                localizer.categoryLabel(type.category(), locale),
                log.getDescription(),
                log.getCreatedTimestamp(),
                localizer.formatTimestamp(log.getCreatedTimestamp(), locale, zone));
    }
}
