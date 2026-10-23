package com.bank.core.notification.controller;

import com.bank.core.notification.domain.NotificationHistoryEntryPoint;
import com.bank.core.notification.domain.NotificationType;
import com.bank.core.notification.dto.NotificationHistoryLabelsResponse;
import com.bank.core.notification.dto.NotificationHistoryQuery;
import com.bank.core.notification.dto.NotificationHistoryResponse;
import com.bank.core.notification.exception.InvalidNotificationHistoryRequestException;
import com.bank.core.notification.security.CustomerClaims;
import com.bank.core.notification.service.NotificationHistoryLocalizer;
import com.bank.core.notification.service.NotificationHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * US-028 notification history API (DS-004). The same endpoint backs every entry point (dashboard, security
 * center, bill payments); the {@code from} parameter only selects the localized return link (FR-001).
 */
@RestController
@RequestMapping("/api/v1/notifications/history")
@Tag(name = "Notification history")
public class NotificationHistoryController {

    private final NotificationHistoryService service;
    private final NotificationHistoryLocalizer localizer;

    public NotificationHistoryController(NotificationHistoryService service, NotificationHistoryLocalizer localizer) {
        this.service = service;
        this.localizer = localizer;
    }

    @GetMapping
    @Operation(summary = "List the signed-in customer's notifications within the retention window, newest first")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History returned; empty list when none exists"),
            @ApiResponse(responseCode = "400", description = "Unknown notification type, entry point or time zone"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"),
            @ApiResponse(responseCode = "403", description = "Token does not identify a retail banking customer")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationHistoryResponse> getHistory(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Notification types to include; all when omitted")
            @RequestParam(name = "types", required = false) List<String> types,
            @Parameter(description = "Customer's selected language: de, fr, it or en")
            @RequestParam(name = "lang", required = false) String language,
            @Parameter(description = "IANA time zone for timestamps, e.g. Europe/Zurich")
            @RequestParam(name = "tz", required = false) String timeZone,
            @Parameter(description = "Entry point: dashboard, security-center or bill-payments")
            @RequestParam(name = "from", required = false) String entryPoint,
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
        Locale locale = localizer.resolve(language, acceptLanguage);
        NotificationHistoryQuery query = new NotificationHistoryQuery(CustomerClaims.customerId(jwt),
                parseTypes(types), locale, parseZone(timeZone), parseEntryPoint(entryPoint));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_LANGUAGE, locale.toLanguageTag())
                .body(service.getHistory(query));
    }

    @GetMapping("/labels")
    @Operation(summary = "Localized labels, headings and system messages for the notification history screen")
    public ResponseEntity<NotificationHistoryLabelsResponse> getLabels(
            @RequestParam(name = "lang", required = false) String language,
            @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
        Locale locale = localizer.resolve(language, acceptLanguage);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_LANGUAGE, locale.toLanguageTag())
                .body(service.getLabels(locale));
    }

    private static Set<NotificationType> parseTypes(List<String> types) {
        EnumSet<NotificationType> parsed = EnumSet.noneOf(NotificationType.class);
        if (types == null) {
            return parsed;
        }
        for (String type : types) {
            if (type.isBlank()) {
                continue;
            }
            try {
                parsed.add(NotificationType.valueOf(type.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                throw new InvalidNotificationHistoryRequestException("Unknown notification type: " + type);
            }
        }
        return parsed;
    }

    private static ZoneId parseZone(String timeZone) {
        if (timeZone == null || timeZone.isBlank()) {
            return null;
        }
        try {
            return ZoneId.of(timeZone.trim());
        } catch (DateTimeException exception) {
            throw new InvalidNotificationHistoryRequestException("Unknown time zone: " + timeZone);
        }
    }

    private static NotificationHistoryEntryPoint parseEntryPoint(String entryPoint) {
        if (entryPoint == null || entryPoint.isBlank()) {
            return null;
        }
        try {
            return NotificationHistoryEntryPoint.parse(entryPoint);
        } catch (IllegalArgumentException exception) {
            throw new InvalidNotificationHistoryRequestException("Unknown entry point: " + entryPoint);
        }
    }
}
