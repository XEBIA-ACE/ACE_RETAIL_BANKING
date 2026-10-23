package com.bank.core.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bank.core.common.exception.GlobalExceptionHandler;
import com.bank.core.config.SecurityConfig;
import com.bank.core.notification.controller.NotificationHistoryController;
import com.bank.core.notification.domain.NotificationHistoryEntryPoint;
import com.bank.core.notification.domain.NotificationType;
import com.bank.core.notification.dto.NotificationHistoryQuery;
import com.bank.core.notification.dto.NotificationHistoryResponse;
import com.bank.core.notification.service.NotificationHistoryLocalizer;
import com.bank.core.notification.service.NotificationHistoryService;
import com.bank.core.support.TestJwtFactory;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationHistoryController.class)
@Import({SecurityConfig.class, NotificationHistoryLocalizer.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class NotificationHistoryControllerTest {

    private static final String HISTORY = "/api/v1/notifications/history";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private NotificationHistoryService service;

    @BeforeEach
    void setUp() {
        when(service.getHistory(any())).thenReturn(
                new NotificationHistoryResponse("en", "UTC", 90, null, null, null, List.of()));
    }

    private static String bearer(String customerId) {
        return "Bearer " + TestJwtFactory.tokenForCustomer(customerId);
    }

    private NotificationHistoryQuery capturedQuery() {
        ArgumentCaptor<NotificationHistoryQuery> query = ArgumentCaptor.forClass(NotificationHistoryQuery.class);
        verify(service).getHistory(query.capture());
        return query.getValue();
    }

    /** [US-028-AC1] The same history is reachable from dashboard, security center and bill payments (FR-001). */
    @ParameterizedTest
    @CsvSource({"dashboard,DASHBOARD", "security-center,SECURITY_CENTER", "bill-payments,BILL_PAYMENTS"})
    void us028Ac1_historyIsReachableFromEveryEntryPoint(String from, NotificationHistoryEntryPoint expected)
            throws Exception {
        mvc.perform(get(HISTORY).param("from", from).header(HttpHeaders.AUTHORIZATION, bearer("cust-a")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        assertThat(capturedQuery().entryPoint()).isEqualTo(expected);
    }

    /** [US-028-AC1] Only signed-in customers can open the history. */
    @Test
    void us028Ac1_requiresSignedInCustomer() throws Exception {
        mvc.perform(get(HISTORY)).andExpect(status().isUnauthorized());
        mvc.perform(get(HISTORY).header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtFactory.tokenWithoutCustomer()))
                .andExpect(status().isForbidden());
        mvc.perform(get(HISTORY).header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    /** [US-028-AC2] Type filters are parsed; unknown types are rejected with 400. */
    @Test
    void us028Ac2_typeFilterIsParsedAndValidated() throws Exception {
        mvc.perform(get(HISTORY).param("types", "low_balance", "BILL_REMINDER")
                        .header(HttpHeaders.AUTHORIZATION, bearer("cust-a")))
                .andExpect(status().isOk());
        assertThat(capturedQuery().types())
                .containsExactlyInAnyOrder(NotificationType.LOW_BALANCE, NotificationType.BILL_REMINDER);

        mvc.perform(get(HISTORY).param("types", "PASSWORD_RESET").header(HttpHeaders.AUTHORIZATION, bearer("cust-a")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid notification history request"));
    }

    /** [US-028-AC4] The customer id comes only from the token; a customerId parameter cannot override it. */
    @Test
    void us028Ac4_customerIdIsTakenFromTokenOnly() throws Exception {
        mvc.perform(get(HISTORY).param("customerId", "cust-b").header(HttpHeaders.AUTHORIZATION, bearer("cust-a")))
                .andExpect(status().isOk());

        assertThat(capturedQuery().customerId()).isEqualTo("cust-a");
    }

    /** [US-028-AC5] Selected language and time zone are passed through; invalid zones are rejected. */
    @Test
    void us028Ac5_languageAndTimeZoneAreResolved() throws Exception {
        mvc.perform(get(HISTORY).param("lang", "it").param("tz", "Europe/Rome")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "de-CH")
                        .header(HttpHeaders.AUTHORIZATION, bearer("cust-a")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_LANGUAGE, "it"));
        NotificationHistoryQuery query = capturedQuery();
        assertThat(query.locale()).isEqualTo(Locale.ITALIAN);
        assertThat(query.timeZone()).isEqualTo(ZoneId.of("Europe/Rome"));

        mvc.perform(get(HISTORY).param("tz", "Mars/Olympus").header(HttpHeaders.AUTHORIZATION, bearer("cust-a")))
                .andExpect(status().isBadRequest());
    }

    /** [US-028-AC1] Unknown entry points are rejected; blank optional parameters fall back to defaults. */
    @Test
    void us028Ac1_entryPointIsValidatedAndBlankParametersAreIgnored() throws Exception {
        mvc.perform(get(HISTORY).param("from", "loans").header(HttpHeaders.AUTHORIZATION, bearer("cust-a")))
                .andExpect(status().isBadRequest());

        mvc.perform(get(HISTORY).param("types", " ").param("tz", " ").param("from", " ")
                        .header(HttpHeaders.AUTHORIZATION, bearer("cust-a")))
                .andExpect(status().isOk());
        NotificationHistoryQuery query = capturedQuery();
        assertThat(query.types()).isEmpty();
        assertThat(query.timeZone()).isNull();
        assertThat(query.entryPoint()).isNull();
    }
}
