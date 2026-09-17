package com.bank.core.account.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bank.core.account.dto.BalanceResponse;
import com.bank.core.account.exception.AccountNotFoundException;
import com.bank.core.account.security.AccountOwnershipChecker;
import com.bank.core.account.service.BalanceService;
import com.bank.core.common.exception.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BalanceController.class)
@Import({com.bank.core.config.SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "security.jwt.secret=test-secret-key-for-banking-core-service-must-be-at-least-64-bytes-long-123456789")
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BalanceService balanceService;
    @MockBean(name = "accountOwnershipChecker")
    private AccountOwnershipChecker ownershipChecker;
    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void returnsBalanceWithIsoTimestamp() throws Exception {
        when(ownershipChecker.check(any(), any())).thenReturn(true);
        when(balanceService.getBalance("account-1")).thenReturn(new BalanceResponse(
                "account-1", new BigDecimal("42.50"), "USD", Instant.parse("2024-01-01T00:00:00Z")));

        mockMvc.perform(get("/api/accounts/account-1/balance").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"accountId":"account-1","balance":42.50,"currency":"USD",
                        "balanceTimestamp":"2024-01-01T00:00:00Z"}
                        """));
    }

    @Test
    void rejectsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/accounts/account-1/balance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsWhenCheckerFalse() throws Exception {
        when(ownershipChecker.check(any(), any())).thenReturn(false);

        mockMvc.perform(get("/api/accounts/account-1/balance").with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void returnsProblemDetailWhenAccountMissing() throws Exception {
        when(ownershipChecker.check(any(), any())).thenReturn(true);
        when(balanceService.getBalance("missing")).thenThrow(new AccountNotFoundException("missing"));

        mockMvc.perform(get("/api/accounts/missing/balance").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }
}
