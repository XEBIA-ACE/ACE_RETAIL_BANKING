package com.bank.core.notification.controller;

import com.bank.core.notification.config.NotificationHistoryProperties;
import com.bank.core.notification.domain.NotificationCategory;
import com.bank.core.notification.domain.NotificationHistoryEntryPoint;
import com.bank.core.notification.dto.NotificationHistoryLabelsResponse;
import com.bank.core.notification.dto.NotificationHistoryQuery;
import com.bank.core.notification.dto.NotificationHistoryResponse;
import com.bank.core.notification.exception.CustomerIdentityMissingException;
import com.bank.core.notification.exception.InvalidNotificationHistoryRequestException;
import com.bank.core.notification.service.NotificationHistoryService;
import com.bank.core.notification.service.SupportedLocales;
import io.swagger.v3.oas.annotations.Operation;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Locale;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notification history API (DS-004). The customer is always taken from the authenticated JWT
 * ({@value #CUSTOMER_ID_CLAIM} claim); no request parameter can select another customer (FR-008, FR-009).
 */
@RestController
@RequestMapping("/api/v1/notifications/history")
public class NotificationHistoryController {

    static final String CUSTOMER_ID_CLAIM = "customer_id";

    private final NotificationHistoryService service;
    private final NotificationHistoryProperties properties;

    public NotificationHistoryController(NotificationHistoryService service,
                                         NotificationHistoryProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @Operation(summary = "List the authenticated customer's notifications within the retention period")
    @GetMapping
    public ResponseEntity<NotificationHistoryResponse> getHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) NotificationCategory category,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(required = false) String lang,
            @RequestParam(name = "tz", required = false) String timeZone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            Locale requestLocale) {
        NotificationHistoryQuery query = new NotificationHistoryQuery(
                customerId(jwt), category, entryPoint(from),
                SupportedLocales.resolve(lang, requestLocale), zoneId(timeZone),
                page, size == null ? properties.defaultPageSize() : size);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.getHistory(query));
    }

    @Operation(summary = "Localized labels for the notification history screen")
    @GetMapping("/labels")
    public NotificationHistoryLabelsResponse getLabels(@RequestParam(required = false) String lang,
                                                       Locale requestLocale) {
        return service.getLabels(SupportedLocales.resolve(lang, requestLocale));
    }

    private static Long customerId(Jwt jwt) {
        Object claim = jwt == null ? null : jwt.getClaim(CUSTOMER_ID_CLAIM);
        if (claim instanceof Number number) {
            return number.longValue();
        }
        if (claim instanceof String text && text.matches("\\d{1,18}")) {
            return Long.valueOf(text);
        }
        throw new CustomerIdentityMissingException();
    }

    private static NotificationHistoryEntryPoint entryPoint(String from) {
        if (from == null || from.isBlank()) {
            return null;
        }
        return NotificationHistoryEntryPoint.fromSlug(from).orElseThrow(() ->
                new InvalidNotificationHistoryRequestException("INVALID_ENTRY_POINT", "error.invalidEntryPoint"));
    }

    private ZoneId zoneId(String timeZone) {
        if (timeZone == null || timeZone.isBlank()) {
            return properties.defaultTimeZone();
        }
        try {
            return ZoneId.of(timeZone);
        } catch (DateTimeException ex) {
            throw new InvalidNotificationHistoryRequestException("INVALID_TIME_ZONE", "error.invalidTimeZone");
        }
    }
}
