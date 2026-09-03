```java
package com.bank.core.dashboard.controller;

import com.bank.core.account.domain.AccountType;
import com.bank.core.account.dto.AccountSummaryDto;
import com.bank.core.account.dto.DashboardResponseDto;
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
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@DisplayName("DashboardController — MockMvc slice tests")
class DashboardControllerTest {

    private static final String ENDPOINT = "/api/v1/dashboard/accounts";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    // -----------------------------------------------------------------------
    // Test 1 — AC-1: Happy path — all account types present
    // -----------------------------------------------------------------------

    @Test
    @WithMockUser(username = "customer-ext-001")
    @DisplayName("AC-1: 200 OK — response contains CHECKING, SAVINGS, and CREDIT_CARD keys")
    void givenAllAccountTypes_whenGetDashboard_thenReturns200WithAllThreeKeys() throws Exception {

        AccountSummaryDto checking = AccountSummaryDto.builder()
                .externalId("acc-uuid-001")
                .accountNumber("****1234")
                .balance(new BigDecimal("1500.00"))
                .currency("USD")
                .status("ACTIVE")
                .nickname("My Main Checking")
                .build();

        AccountSummaryDto savings = AccountSummaryDto.builder()
                .externalId("acc-uuid-002")
                .accountNumber("****5678")
                .balance(new BigDecimal("3000.00"))
                .currency("USD")
                .status("ACTIVE")
                .nickname("Emergency Fund")
                .build();

        AccountSummaryDto creditCard = AccountSummaryDto.builder()
                .externalId("acc-uuid-003")
                .accountNumber("****9012")
                .balance(new BigDecimal("-250.00"))
                .currency("USD")
                .status("ACTIVE")
                .nickname(null)
                .build();

        DashboardResponseDto response = DashboardResponseDto.builder()
                .customerId("customer-ext-001")
                .accounts(Map.of(
                        AccountType.CHECKING, List.of(checking),
                        AccountType.SAVINGS, List.of(savings),
                        AccountType.CREDIT_CARD, List.of(creditCard)
                ))
                .build();

        when(accountService.getDashboardForCustomer(anyString())).thenReturn(response);

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts.CHECKING").isArray())
                .andExpect(jsonPath("$.accounts.CHECKING", hasSize(1)))
                .andExpect(jsonPath("$.accounts.CHECKING[0].externalId").value("acc-uuid-001"))
                .andExpect(jsonPath("$.accounts.CHECKING[0].accountNumber").value("****1234"))
                .andExpect(jsonPath("$.accounts.CHECKING[0].balance").value(1500.00))
                .andExpect(jsonPath("$.accounts.CHECKING[0].currency").value("USD"))
                .andExpect(jsonPath("$.accounts.CHECKING[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.accounts.CHECKING[0].nickname").value("My Main Checking"))
                .andExpect(jsonPath("$.accounts.SAVINGS").isArray())
                .andExpect(jsonPath("$.accounts.SAVINGS", hasSize(1)))
                .andExpect(jsonPath("$.accounts.SAVINGS[0].externalId").value("acc-uuid-002"))
                .andExpect(jsonPath("$.accounts.CREDIT_CARD").isArray())
                .andExpect(jsonPath("$.accounts.CREDIT_CARD", hasSize(1)))
                .andExpect(jsonPath("$.accounts.CREDIT_CARD[0].externalId").value("acc-uuid-003"));
    }

    // -----------------------------------------------------------------------
    // Test 2 — AC-2: Absent account type — no empty placeholders
    // -----------------------------------------------------------------------

    @Test
    @WithMockUser(username = "customer-ext-002")
    @DisplayName("AC-2: 200 OK — absent account types (SAVINGS, CREDIT_CARD) are not present in response")
    void givenOnlyCheckingAccount_whenGetDashboard_thenSavingsAndCreditCardKeysAbsent() throws Exception {

        AccountSummaryDto checking = AccountSummaryDto.builder()
                .externalId("acc-uuid-010")
                .accountNumber("****4321")
                .balance(new BigDecimal("500.00"))
                .currency("USD")
                .status("ACTIVE")
                .nickname(null)
                .build();

        DashboardResponseDto response = DashboardResponseDto.builder()
                .customerId("customer-ext-002")
                .accounts(Map.of(AccountType.CHECKING, List.of(checking)))
                .build();

        when(accountService.getDashboardForCustomer(anyString())).thenReturn(response);

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts.CHECKING").isArray())
                .andExpect(jsonPath("$.accounts.SAVINGS").doesNotExist())
                .andExpect(jsonPath("$.accounts.CREDIT_CARD").doesNotExist());
    }

    // -----------------------------------------------------------------------
    // Test 3 — AC-3: Non-active account is included with correct status
    // -----------------------------------------------------------------------

    @Test
    @WithMockUser(username = "customer-ext-003")
    @DisplayName("AC-3: 200 OK — non-active account is included in response with INACTIVE status")
    void givenInactiveAccount_whenGetDashboard_thenAccountIncludedWithInactiveStatus() throws Exception {

        AccountSummaryDto inactiveCard = AccountSummaryDto.builder()
                .externalId("acc-uuid-020")
                .accountNumber("****7777")
                .balance(new BigDecimal("-100.00"))
                .currency("USD")
                .status("INACTIVE")
                .nickname(null)
                .build();

        DashboardResponseDto response = DashboardResponseDto.builder()
                .customerId("customer-ext-003")
                .accounts(Map.of(AccountType.CREDIT_CARD, List.of(inactiveCard)))
                .build();

        when(accountService.getDashboardForCustomer(anyString())).thenReturn(response);

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts.CREDIT_CARD").isArray())
                .andExpect(jsonPath("$.accounts.CREDIT_CARD", hasSize(1)))
                .andExpect(jsonPath("$.accounts.CREDIT_CARD[0].externalId").value("acc-uuid-020"))
                .andExpect(jsonPath("$.accounts.CREDIT_CARD[0].status").value("INACTIVE"));
    }

    // -----------------------------------------------------------------------
    // Test 4 — AC-4: Single account type — no placeholders for others
    // -----------------------------------------------------------------------

    @Test
    @WithMockUser(username = "customer-ext-004")
    @DisplayName("AC-4: 200 OK — only SAVINGS key present when customer holds only savings accounts")
    void givenOnlySavingsAccount_whenGetDashboard_thenOnlySavingsKeyPresent() throws Exception {

        AccountSummaryDto savings = AccountSummaryDto.builder()
                .externalId("acc-uuid-030")
                .accountNumber("****8888")
                .balance(new BigDecimal("10000.00"))
                .currency("USD")
                .status("ACTIVE")
                .nickname("Long-term Savings")
                .build();

        DashboardResponseDto response = DashboardResponseDto.builder()
                .customerId("customer-ext-004")
                .accounts(Map.of(AccountType.SAVINGS, List.of(savings)))
                .build();

        when(accountService.getDashboardForCustomer(anyString())).thenReturn(response);

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts.SAVINGS").isArray())
                .andExpect(jsonPath("$.accounts.SAVINGS", hasSize(1)))
                .andExpect(jsonPath("$.accounts.SAVINGS[0].externalId").value("acc-uuid-030"))
                .andExpect(jsonPath("$.accounts.CHECKING").doesNotExist())
                .andExpect(jsonPath("$.accounts.CREDIT_CARD").doesNotExist());
    }

    // -----------------------------------------------------------------------
    // Test 5 — 401: Unauthenticated request
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("401 Unauthorized — request without JWT / authentication is rejected")
    void givenNoAuthentication_whenGetDashboard_thenReturns401() throws Exception {

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Test 6 — Response structure: customerId field present
    // -----------------------------------------------------------------------

    @Test
    @WithMockUser(username = "customer-ext-005")
    @DisplayName("Response structure: customerId field is present in the response body")
    void givenAuthenticatedCustomer_whenGetDashboard_thenResponseContainsCustomerId() throws Exception {

        AccountSummaryDto checking = AccountSummaryDto.builder()
                .externalId("acc-uuid-040")
                .accountNumber("****1111")
                .balance(new BigDecimal("200.00"))
                .currency("USD")
                .status("ACTIVE")
                .nickname(null)
                .build();

        DashboardResponseDto response = DashboardResponseDto.builder()
                .customerId("customer-ext-005")
                .accounts(Map.of(AccountType.CHECKING, List.of(checking)))
                .build();

        when(accountService.getDashboardForCustomer(anyString())).thenReturn(response);

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").exists())
                .andExpect(jsonPath("$.customerId").isNotEmpty())
                .andExpect(jsonPath("$.customerId").value("customer-ext-005"));
    }

    // -----------------------------------------------------------------------
    // Test 7 — Account number masking: ****XXXX pattern
    // -----------------------------------------------------------------------

    @Test
    @WithMockUser(username = "customer-ext-006")
    @DisplayName("Account number masking: accountNumber in response matches ****XXXX pattern")
    void givenAuthenticatedCustomer_whenGetDashboard_thenAccountNumberIsMasked() throws Exception {

        AccountSummaryDto checking = AccountSummaryDto.builder()
                .externalId("acc-uuid-050")
                .accountNumber("****2345")
                .balance(new BigDecimal("750.00"))
                .currency("USD")
                .status("ACTIVE")
                .nickname("Salary Account")
                .build();

        AccountSummaryDto savings = AccountSummaryDto.builder()
                .externalId("acc-uuid-051")
                .accountNumber("****6789")
                .balance(new BigDecimal("5000.00"))
                .currency("GBP")
                .status("ACTIVE")
                .nickname(null)
                .build();

        DashboardResponseDto response = DashboardResponseDto.builder()
                .customerId("customer-ext-006")
                .accounts(Map.of(
                        AccountType.CHECKING, List.of(checking),
                        AccountType.SAVINGS, List.of(savings)
                ))
                .build();

        when(accountService.getDashboardForCustomer(anyString())).thenReturn(response);

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts.CHECKING[0].accountNumber").value(matchesPattern("\\*{4}\\d{4}")))
                .andExpect(jsonPath("$.accounts.SAVINGS[0].accountNumber").value(matchesPattern("\\*{4}\\d{4}")));
    }
}
```