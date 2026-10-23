package com.bank.core.notification.controller;

import com.bank.core.notification.dto.NotificationHistoryResponse;
import com.bank.core.notification.security.CustomerClaims;
import com.bank.core.notification.service.NotificationHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification history", description = "US-009 smart notification history for the signed-in customer")
public class NotificationHistoryController {

    private final NotificationHistoryService service;

    @GetMapping("/history")
    @Operation(summary = "List the caller's smart notification history, most recent first")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History returned; empty list when none exists"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"),
            @ApiResponse(responseCode = "403", description = "Token does not identify a retail banking customer")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationHistoryResponse> getHistory(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.getHistory(CustomerClaims.customerId(jwt)));
    }
}
