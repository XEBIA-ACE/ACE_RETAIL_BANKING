```java
package com.bank.core.account.controller;

import com.bank.core.account.dto.AccountSummaryDto;
import com.bank.core.account.dto.DashboardResponse;
import com.bank.core.account.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    private static final String DASHBOARD_URL = "/api/v1/accounts/dashboard";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    // -------------------------------------------------------------------------
    // Scenario 1 — 200 with accounts
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "customer1")
    @DisplayName("GET /dashboard returns 200 with accounts list when customer has accounts")
    void whenAuthenticatedAndHasAccounts_thenReturns200WithAllFields() throws Exception {
        AccountSummaryDto account1 = AccountSummaryDto.builder()
                .accountId(UUID.fromString("a1b2c3d4-0000-0000-0000-000000000001").toString())
                .accountTypeName("Checking Account")
                .maskedAccountNumber("****4321")
                .currentBalance(new BigDecimal("1250.00"))
                .currencyCode("USD")
                .ariaLabel("Checking Account ending in 4321")
                .build();

        AccountSummaryDto account2 = AccountSummaryDto.builder()
                .accountId(UUID.fromString("e5f6g7h8-0000-0000-0000-000000000002").toString())
                .accountTypeName("Savings Account")
                .maskedAccountNumber("****8765")
                .currentBalance(new BigDecimal("5400.50"))
                .currencyCode("USD")
                .ariaLabel("Savings Account ending in 8765")
                .build();

        DashboardResponse response = DashboardResponse.builder()
                .hasAccounts(true)
                .emptyStateMessage(null)
                .accounts(List.of(account1, account2))
                .build();

        when(accountService.getDashboard(any())).thenReturn(response);

        mockMvc.perform(get(DASHBOARD_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasAccounts", is(true)))
                .andExpect(jsonPath("$.accounts", hasSize(2)))
                // account 1 fields
                .andExpect(jsonPath("$.accounts[0].accountId",
                        is("a1b2c3d4-0000-0000-0000-000000000001")))
                .andExpect(jsonPath("$.accounts[0].accountTypeName",
                        is("Checking Account")))
                .andExpect(jsonPath("$.accounts[0].maskedAccountNumber",
                        is("****4321")))
                .andExpect(jsonPath("$.accounts[0].currentBalance",
                        comparesEqualTo(1250.00)))
                .andExpect(jsonPath("$.accounts[0].currencyCode",
                        is("USD")))
                .andExpect(jsonPath("$.accounts[0].ariaLabel",
                        is("Checking Account ending in 4321")))
                // account 2 fields
                .andExpect(jsonPath("$.accounts[1].accountId",
                        is("e5f6g7h8-0000-0000-0000-000000000002")))
                .andExpect(jsonPath("$.accounts[1].accountTypeName",
                        is("Savings Account")))
                .andExpect(jsonPath("$.accounts[1].maskedAccountNumber",
                        is("****8765")))
                .andExpect(jsonPath("$.accounts[1].currentBalance",
                        comparesEqualTo(5400.50)))
                .andExpect(jsonPath("$.accounts[1].currencyCode",
                        is("USD")))
                .andExpect(jsonPath("$.accounts[1].ariaLabel",
                        is("Savings Account ending in 8765")));
    }

    // -------------------------------------------------------------------------
    // Scenario 2 — 200 empty state
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "customer2")
    @DisplayName("GET /dashboard returns 200 with empty state when customer has no accounts")
    void whenAuthenticatedAndNoAccounts_thenReturns200WithEmptyState() throws Exception {
        DashboardResponse emptyResponse = DashboardResponse.builder()
                .hasAccounts(false)
                .emptyStateMessage(
                        "You have no linked accounts. Please visit a branch or contact support to link your accounts.")
                .accounts(List.of())
                .build();

        when(accountService.getDashboard(any())).thenReturn(emptyResponse);

        mockMvc.perform(get(DASHBOARD_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasAccounts", is(false)))
                .andExpect(jsonPath("$.accounts", hasSize(0)))
                .andExpect(jsonPath("$.emptyStateMessage", not(emptyOrNullString())));
    }

    // -------------------------------------------------------------------------
    // Scenario 3 — 401 unauthenticated
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /dashboard returns 401 when request is unauthenticated")
    void whenUnauthenticated_thenReturns401() throws Exception {
        mockMvc.perform(get(DASHBOARD_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accounts").doesNotExist())
                .andExpect(jsonPath("$.hasAccounts").doesNotExist());
    }

    // -------------------------------------------------------------------------
    // Scenario 4 — ariaLabel format
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "customer3")
    @DisplayName("ariaLabel matches '{accountTypeName} ending in {last4}' pattern")
    void whenAuthenticatedAndHasAccounts_thenAriaLabelMatchesExpectedPattern() throws Exception {
        AccountSummaryDto account = AccountSummaryDto.builder()
                .accountId(UUID.randomUUID().toString())
                .accountTypeName("Credit Card")
                .maskedAccountNumber("****9999")
                .currentBalance(new BigDecimal("0.00"))
                .currencyCode("USD")
                .ariaLabel("Credit Card ending in 9999")
                .build();

        DashboardResponse response = DashboardResponse.builder()
                .hasAccounts(true)
                .emptyStateMessage(null)
                .accounts(List.of(account))
                .build();

        when(accountService.getDashboard(any())).thenReturn(response);

        mockMvc.perform(get(DASHBOARD_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[0].ariaLabel",
                        matchesPattern("^.+ ending in \\d{4}$")))
                .andExpect(jsonPath("$.accounts[0].ariaLabel",
                        is(account.getAccountTypeName() + " ending in "
                                + account.getMaskedAccountNumber().substring(4))));
    }

    // -------------------------------------------------------------------------
    // Scenario 5 — No full account number in response
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "customer4")
    @DisplayName("Response body does not contain the unmasked account number")
    void whenAuthenticatedAndHasAccounts_thenFullAccountNumberNotInResponse() throws Exception {
        String fullAccountNumber = "1234567890004321";
        String last4 = "4321";

        AccountSummaryDto account = AccountSummaryDto.builder()
                .accountId(UUID.randomUUID().toString())
                .accountTypeName("Checking Account")
                .maskedAccountNumber("****" + last4)
                .currentBalance(new BigDecimal("500.00"))
                .currencyCode("USD")
                .ariaLabel("Checking Account ending in " + last4)
                .build();

        DashboardResponse response = DashboardResponse.builder()
                .hasAccounts(true)
                .emptyStateMessage(null)
                .accounts(List.of(account))
                .build();

        when(accountService.getDashboard(any())).thenReturn(response);

        mockMvc.perform(get(DASHBOARD_URL))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(fullAccountNumber))))
                .andExpect(jsonPath("$.accounts[0].maskedAccountNumber",
                        startsWith("****")))
                .andExpect(jsonPath("$.accounts[0].maskedAccountNumber",
                        not(containsString(fullAccountNumber))));
    }
}
```