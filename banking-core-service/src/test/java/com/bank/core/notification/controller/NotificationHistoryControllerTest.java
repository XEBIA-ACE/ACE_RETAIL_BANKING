package com.bank.core.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bank.core.config.SecurityConfig;
import com.bank.core.notification.domain.DeliveryStatus;
import com.bank.core.notification.dto.NotificationHistoryItemDto;
import com.bank.core.notification.dto.NotificationHistoryResponse;
import com.bank.core.notification.dto.RecordNotificationResult;
import com.bank.core.notification.exception.NotificationHistoryConflictException;
import com.bank.core.notification.service.NotificationHistoryService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({NotificationHistoryController.class, NotificationDeliveryController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties =
        "security.jwt.secret=test-secret-key-for-banking-core-service-must-be-at-least-64-bytes-long-123456789")
class NotificationHistoryControllerTest {

    private static final Instant NOW = Instant.parse("2026-10-20T10:00:00Z");
    private static final String VALID_BODY = """
            {"customerId":"cust-1","notificationType":"LOW_BALANCE","message":"Balance below 100.00",
             "notificationTimestamp":"2026-10-20T10:00:00Z","deliveryStatus":"DELIVERED",
             "deliveredAt":"2026-10-20T10:00:01Z"}
            """;

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private NotificationHistoryService service;

    /** [US-009-ac-1] FR-001: authenticated customer receives own history, most recent first. */
    @Test
    void us009Ac1_returnsHistoryForAuthenticatedCustomer() throws Exception {
        when(service.getHistory("cust-1")).thenReturn(new NotificationHistoryResponse(List.of(
                item("n-2", "LOW_BALANCE", NOW), item("n-1", "LARGE_TRANSACTION", NOW.minusSeconds(60)))));

        mockMvc.perform(get("/api/v1/notifications/history").with(customer("cust-1")))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.notifications[0].notificationId").value("n-2"))
                .andExpect(jsonPath("$.notifications[0].notificationTimestamp").value("2026-10-20T10:00:00Z"))
                .andExpect(jsonPath("$.notifications[1].notificationId").value("n-1"));
    }

    /** [US-009-ac-1] FR-001: the screen's API is accessible only to authenticated customers. */
    @Test
    void us009Ac1_rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/history")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    /** [US-009-ac-1] FR-001: a token that does not identify a retail customer is refused. */
    @Test
    void us009Ac1_rejectsTokenWithoutCustomerClaim() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/history").with(jwt()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    /** [US-009-ac-2] FR-002: no history returns 200 with an empty collection, never an error. */
    @Test
    void us009Ac2_returnsEmptyCollection() throws Exception {
        when(service.getHistory("cust-1")).thenReturn(new NotificationHistoryResponse(List.of()));

        mockMvc.perform(get("/api/v1/notifications/history").with(customer("cust-1")))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"notifications\":[]}", true));
    }

    /** [US-009-ac-3] FR-003: delivery completion creates a history record. */
    @Test
    void us009Ac3_recordsDeliveredNotification() throws Exception {
        when(service.recordDelivered(eq("src-1"), any()))
                .thenReturn(new RecordNotificationResult(item("n-1", "LOW_BALANCE", NOW), true));

        mockMvc.perform(put("/internal/v1/notification-history/src-1").with(writer())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notificationId").value("n-1"));
    }

    /** [US-009-ac-3] T002: replay of an already-recorded notification is idempotent (200). */
    @Test
    void us009Ac3_replayReturnsOk() throws Exception {
        when(service.recordDelivered(eq("src-1"), any()))
                .thenReturn(new RecordNotificationResult(item("n-1", "LOW_BALANCE", NOW), false));

        mockMvc.perform(put("/internal/v1/notification-history/src-1").with(writer())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk());
    }

    @Test
    void us009Ac3_rejectsCallerWithoutWriteScope() throws Exception {
        mockMvc.perform(put("/internal/v1/notification-history/src-1").with(customer("cust-1"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void us009Ac3_rejectsInvalidPayload() throws Exception {
        mockMvc.perform(put("/internal/v1/notification-history/src-1").with(writer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"customerId\":\"cust-1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        verifyNoInteractions(service);
    }

    @Test
    void us009Ac3_rejectsInvalidSourceNotificationId() throws Exception {
        mockMvc.perform(put("/internal/v1/notification-history/bad id!").with(writer())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void us009Ac3_returnsConflictWhenIdentifierBelongsToAnotherCustomer() throws Exception {
        when(service.recordDelivered(eq("src-1"), any())).thenThrow(new NotificationHistoryConflictException("src-1"));

        mockMvc.perform(put("/internal/v1/notification-history/src-1").with(writer())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor customer(String customerId) {
        return jwt().jwt(token -> token.claim("customerId", customerId));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor writer() {
        return jwt().authorities(new SimpleGrantedAuthority(SecurityConfig.NOTIFICATION_HISTORY_WRITE));
    }

    private static NotificationHistoryItemDto item(String id, String type, Instant at) {
        return new NotificationHistoryItemDto(id, "cust-1", type, "message " + id, at,
                DeliveryStatus.DELIVERED, at.plusSeconds(1), null);
    }
}
