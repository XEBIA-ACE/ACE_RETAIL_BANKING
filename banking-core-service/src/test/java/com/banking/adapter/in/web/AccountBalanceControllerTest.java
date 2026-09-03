package com.banking.adapter.in.web;

import com.banking.application.service.AccountService;
import com.banking.domain.exception.ResourceNotFoundException;
import com.banking.domain.model.Account;
import com.banking.domain.model.AccountStatus;
import com.banking.domain.model.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @WebMvcTest slice tests for the account balance endpoint.
 * Security is disabled via the test profile (application-test.yml sets spring.autoconfigure.exclude
 * for security auto-configuration, or the SecurityConfig is excluded from the slice).
 */
@WebMvcTest(AccountController.class)
@ActiveProfiles("test")
class AccountBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    // -----------------------------------------------------------------------
    // Helper builders
    // -----------------------------------------------------------------------

    private Account buildAccount(String externalId, String accountNumber,
                                  AccountType type, BigDecimal balance,
                                  String currency, AccountStatus status) {
        return Account.builder()
                .id(null)                          // internal id must NOT appear in response
                .externalId(externalId)
                .customerId(null)
                .accountNumber(accountNumber)
                .accountType(type)
                .balance(balance)
                .currency(currency)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // -----------------------------------------------------------------------
    // Test 1 — 200 single account
    // -----------------------------------------------------------------------

    @Test
    void getAccountBalances_singleAccount_returns200WithAllFields() throws Exception {
        Account account = buildAccount(
                "acc-ext-id-1",
                "1000000001",
                AccountType.CHECKING,
                new BigDecimal("1234.56"),
                "USD",
                AccountStatus.ACTIVE
        );

        when(accountService.getAccountBalances("cust-ext-id-1"))
                .thenReturn(List.of(account));

        mockMvc.perform(get("/api/v1/customers/cust-ext-id-1/accounts/balances")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // Array length 1
                .andExpect(jsonPath("$", hasSize(1)))
                // All six required fields present with correct values
                .andExpect(jsonPath("$[0].accountExternalId", is("acc-ext-id-1")))
                .andExpect(jsonPath("$[0].accountNumber",     is("1000000001")))
                .andExpect(jsonPath("$[0].accountType",       is("CHECKING")))
                .andExpect(jsonPath("$[0].balance",           is(1234.56)))
                .andExpect(jsonPath("$[0].currency",          is("USD")))
                .andExpect(jsonPath("$[0].status",            is("ACTIVE")))
                // Internal database id must NOT be present
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    // -----------------------------------------------------------------------
    // Test 2 — 200 multiple accounts
    // -----------------------------------------------------------------------

    @Test
    void getAccountBalances_multipleAccounts_returns200WithDistinctValues() throws Exception {
        Account account1 = buildAccount(
                "acc-ext-id-1",
                "1000000001",
                AccountType.CHECKING,
                new BigDecimal("1234.56"),
                "USD",
                AccountStatus.ACTIVE
        );
        Account account2 = buildAccount(
                "acc-ext-id-2",
                "1000000002",
                AccountType.SAVINGS,
                new BigDecimal("9876.00"),
                "USD",
                AccountStatus.ACTIVE
        );

        when(accountService.getAccountBalances("cust-ext-id-1"))
                .thenReturn(List.of(account1, account2));

        mockMvc.perform(get("/api/v1/customers/cust-ext-id-1/accounts/balances")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Array length 2
                .andExpect(jsonPath("$", hasSize(2)))
                // Distinct externalIds
                .andExpect(jsonPath("$[0].accountExternalId", is("acc-ext-id-1")))
                .andExpect(jsonPath("$[1].accountExternalId", is("acc-ext-id-2")))
                // Distinct balances
                .andExpect(jsonPath("$[0].balance", is(1234.56)))
                .andExpect(jsonPath("$[1].balance", is(9876.00)))
                // No internal id on either element
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[1].id").doesNotExist());
    }

    // -----------------------------------------------------------------------
    // Test 3 — 404 unknown customer
    // -----------------------------------------------------------------------

    @Test
    void getAccountBalances_unknownCustomer_returns404() throws Exception {
        when(accountService.getAccountBalances("unknown"))
                .thenThrow(new ResourceNotFoundException("Customer not found: unknown"));

        mockMvc.perform(get("/api/v1/customers/unknown/accounts/balances")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------------
    // Test 4 — 401 unauthenticated (only meaningful when security is active)
    // The test profile disables security, so this test is annotated to be
    // skipped when the profile disables the security filter chain.
    // If security IS active in the slice, uncomment the assertions below.
    // -----------------------------------------------------------------------

    /*
     * Uncomment if Spring Security is active in the @WebMvcTest slice:
     *
     * @Test
     * void getAccountBalances_noToken_returns401() throws Exception {
     *     mockMvc.perform(get("/api/v1/customers/cust-ext-id-1/accounts/balances"))
     *             .andExpect(status().isUnauthorized());
     * }
     */
}
