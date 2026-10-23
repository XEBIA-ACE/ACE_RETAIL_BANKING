package com.bank.core.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bank.core.config.SecurityConfig;
import com.bank.core.notification.config.NotificationHistoryConfig;
import com.bank.core.notification.config.NotificationHistoryProperties;
import com.bank.core.notification.controller.NotificationHistoryController;
import com.bank.core.notification.domain.NotificationCategory;
import com.bank.core.notification.domain.NotificationHistoryEntryPoint;
import com.bank.core.notification.dto.NotificationHistoryQuery;
import com.bank.core.notification.service.NotificationHistoryService;
import java.time.ZoneId;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(NotificationHistoryController.class)
@Import({SecurityConfig.class, NotificationHistoryConfig.class})
@EnableConfigurationProperties(NotificationHistoryProperties.class)
@ActiveProfiles("test")
class NotificationHistoryControllerTest {

    private static final String URL = "/api/v1/notifications/history";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private NotificationHistoryService service;

    private static MockHttpServletRequestBuilder asCustomer(long customerId) {
        return get(URL).with(jwt().jwt(j -> j.claim("customer_id", customerId)));
    }

    private NotificationHistoryQuery capturedQuery() {
        ArgumentCaptor<NotificationHistoryQuery> captor = ArgumentCaptor.forClass(NotificationHistoryQuery.class);
        verify(service).getHistory(captor.capture());
        return captor.getValue();
    }

    @ParameterizedTest
    @EnumSource(NotificationHistoryEntryPoint.class)
    void us028Ac1_historyIsReachableFromEveryEntryPoint(NotificationHistoryEntryPoint entryPoint) throws Exception {
        mvc.perform(asCustomer(7).param("from", entryPoint.getSlug()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"));

        assertThat(capturedQuery().entryPoint()).isEqualTo(entryPoint);
    }

    @Test
    void us028Ac1_unknownEntryPointIsRejected() throws Exception {
        mvc.perform(asCustomer(7).param("from", "admin"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ENTRY_POINT"));
        verifyNoInteractions(service);
    }

    @Test
    void us028Ac2_categoryFilterIsPassedToService() throws Exception {
        mvc.perform(asCustomer(7).param("category", "BILL")).andExpect(status().isOk());

        assertThat(capturedQuery().category()).isEqualTo(NotificationCategory.BILL);
    }

    @Test
    void us028Ac2_unknownCategoryIsRejected() throws Exception {
        mvc.perform(asCustomer(7).param("category", "LOANS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void us028Ac4_customerComesFromTokenAndRequestParametersCannotOverrideIt() throws Exception {
        mvc.perform(asCustomer(7).param("customerId", "99").param("customer_id", "99"))
                .andExpect(status().isOk());

        assertThat(capturedQuery().customerId()).isEqualTo(7L);
    }

    @Test
    void us028Ac4_unauthenticatedRequestIsRejected() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void us028Ac4_tokenWithoutCustomerClaimIsForbidden() throws Exception {
        mvc.perform(get(URL).param("lang", "de").with(jwt().jwt(j -> j.claim("customer_id", "abc"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CUSTOMER_IDENTITY_MISSING"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Kunden")));
        verifyNoInteractions(service);
    }

    @Test
    void us028Ac5_languageAndTimeZoneAreHonoured() throws Exception {
        mvc.perform(asCustomer(7).param("lang", "it").param("tz", "Europe/Rome")).andExpect(status().isOk());

        NotificationHistoryQuery query = capturedQuery();
        assertThat(query.locale()).isEqualTo(Locale.ITALIAN);
        assertThat(query.zoneId()).isEqualTo(ZoneId.of("Europe/Rome"));
    }

    @Test
    void us028Ac5_acceptLanguageAndDefaultZoneAreUsedWhenNoParameters() throws Exception {
        mvc.perform(asCustomer(7).header("Accept-Language", "fr-CH")).andExpect(status().isOk());

        NotificationHistoryQuery query = capturedQuery();
        assertThat(query.locale().getLanguage()).isEqualTo("fr");
        assertThat(query.zoneId()).isEqualTo(ZoneId.of("Europe/Zurich"));
        assertThat(query.size()).isEqualTo(20);
    }

    @Test
    void us028Ac5_invalidTimeZoneReturnsLocalizedError() throws Exception {
        mvc.perform(asCustomer(7).param("tz", "Mars/Olympus").param("lang", "fr"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_TIME_ZONE"))
                .andExpect(jsonPath("$.message").value("Le fuseau horaire sélectionné n'est pas pris en charge."));
    }

    @Test
    void us028Ac5_labelsAreAvailableBeforeSignInForTheScreenChrome() throws Exception {
        mvc.perform(get(URL + "/labels").param("lang", "de")).andExpect(status().isOk());

        verify(service).getLabels(any(Locale.class));
    }
}
