package com.bank.core.notification.controller;

import com.bank.core.notification.dto.NotificationDeliveredRequest;
import com.bank.core.notification.dto.NotificationHistoryItemDto;
import com.bank.core.notification.dto.RecordNotificationResult;
import com.bank.core.notification.service.NotificationHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Internal integration point used by the Smart Notifications capability (US-009 T002, FR-003). */
@RestController
@RequestMapping("/internal/v1/notification-history")
@RequiredArgsConstructor
@Tag(name = "Notification history (internal)", description = "Delivery-completion hook for Smart Notifications")
public class NotificationDeliveryController {

    private final NotificationHistoryService service;

    @PutMapping("/{sourceNotificationId}")
    @Operation(summary = "Idempotently record a notification whose delivery processing has completed")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notification added to the customer's history"),
            @ApiResponse(responseCode = "200", description = "Notification was already recorded (replay)"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "403", description = "Caller lacks notification-history:write scope"),
            @ApiResponse(responseCode = "409", description = "Identifier already used for another customer")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationHistoryItemDto> recordDelivered(
            @PathVariable @Pattern(regexp = "^[A-Za-z0-9._:-]{1,100}$") String sourceNotificationId,
            @Valid @RequestBody NotificationDeliveredRequest request) {
        RecordNotificationResult result = service.recordDelivered(sourceNotificationId, request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.notification());
    }
}
