```java
package com.bank.core.dashboard.controller;

import com.bank.core.account.domain.AccountStatus;
import com.bank.core.account.domain.AccountType;
import com.bank.core.account.dto.AccountSummaryDto;
import com.bank.core.account.dto.DashboardResponseDto;
import com.bank.core.account.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance validation test for GET /api/v1/dashboard/accounts.
 *
 * <p>Approach: Option B (MockMvc timing test).
 *
 * <p><strong>Limitation note:</strong> This test uses MockMvc with a mocked service that
 * introduces no artificial delay. It therefore measures only Spring MVC framework overhead
 * (dispatcher servlet, filter chain, serialisation) and does NOT reflect real database or
 * network latency. The p95 target of 2 000 ms is intentionally conservative so that the
 * test reliably passes in CI while still catching catastrophic regressions in framework
 * overhead (e.g., a misconfigured security filter that blocks and retries, or a serialiser
 * that performs expensive reflection on every call).
 *
 * <p>For a full end-to-end performance validation against a real database, use the
 * Testcontainers-based integration test (Option A) in a dedicated performance environment.
 *
 * <p>NFR reference: NFR-001 — API p95 response time ≤ 2 000 ms.
 */
@Tag("performance")
@WebMvcTest(DashboardController.class)
@DisplayName("NFR-001 — Dashboard API p95 response time ≤ 2 000 ms (MockMvc framework overhead)")
class DashboardPerformanceTest {

    private static final String ENDPOINT = "/api/v1/dashboard/accounts";
    private static final int INVOCATION_COUNT = 20;
    private static final long P95_THRESHOLD_MS = 2_000L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @BeforeEach
    void stubAccountService() {
        // Build a realistic response: 3 account types, multiple accounts each.
        List<AccountSummaryDto> checkingAccounts = buildAccounts("CHECKING", 5);
        List<AccountSummaryDto> savingsAccounts = buildAccounts("SAVINGS", 3);
        List<AccountSummaryDto> creditCardAccounts = buildAccounts("CREDIT_CARD", 2);

        DashboardResponseDto response = DashboardResponseDto.builder()
                .customerId("ext-uuid-perf-test")
                .accounts(Map.of(
                        AccountType.CHECKING, checkingAccounts,
                        AccountType.SAVINGS, savingsAccounts,
                        AccountType.CREDIT_CARD, creditCardAccounts
                ))
                .build();

        when(accountService.getAccountsByCustomer(anyString())).thenReturn(response);
    }

    @Test
    @WithMockUser(username = "perf-test-user", roles = "USER")
    @DisplayName("AC-5 / NFR-001: p95 response time must be under 2 000 ms across 20 invocations")
    void dashboardEndpoint_p95ResponseTime_isUnderThreshold() throws Exception {
        List<Long> responseTimes = new ArrayList<>(INVOCATION_COUNT);

        // Warm-up: one call before measurement to initialise Spring internals.
        mockMvc.perform(get(ENDPOINT).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Measurement phase.
        for (int i = 0; i < INVOCATION_COUNT; i++) {
            long start = System.nanoTime();

            MvcResult result = mockMvc.perform(get(ENDPOINT).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
            responseTimes.add(elapsedMs);

            // Ensure the response body is non-empty (guards against a trivially fast empty response).
            assertThat(result.getResponse().getContentAsString()).isNotBlank();
        }

        long p95Ms = computeP95(responseTimes);

        System.out.printf(
                "[PERFORMANCE] GET %s — %d invocations | p95 = %d ms | threshold = %d ms%n",
                ENDPOINT, INVOCATION_COUNT, p95Ms, P95_THRESHOLD_MS
        );
        System.out.printf(
                "[PERFORMANCE] All response times (ms): %s%n",
                responseTimes
        );

        assertThat(p95Ms)
                .as("p95 response time (%d ms) must be under NFR-001 threshold of %d ms", p95Ms, P95_THRESHOLD_MS)
                .isLessThanOrEqualTo(P95_THRESHOLD_MS);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Computes the 95th percentile of the supplied list of response times.
     *
     * <p>Uses the "nearest rank" method: sort ascending, take the value at
     * index {@code ceil(0.95 * n) - 1}.
     */
    private static long computeP95(List<Long> times) {
        List<Long> sorted = new ArrayList<>(times);
        Collections.sort(sorted);
        int index = (int) Math.ceil(0.95 * sorted.size()) - 1;
        // Guard against rounding to -1 for very small lists (should not happen here).
        index = Math.max(0, index);
        return sorted.get(index);
    }

    /**
     * Builds a list of {@code count} {@link AccountSummaryDto} instances for the given
     * account type string. Alternates between ACTIVE and INACTIVE statuses to exercise
     * the full response shape.
     */
    private static List<AccountSummaryDto> buildAccounts(String accountTypeLabel, int count) {
        List<AccountSummaryDto> accounts = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            AccountStatus status = (i % 2 == 0) ? AccountStatus.INACTIVE : AccountStatus.ACTIVE;
            accounts.add(AccountSummaryDto.builder()
                    .externalId("ext-" + accountTypeLabel.toLowerCase() + "-" + i)
                    .accountNumber("****" + String.format("%04d", i))
                    .balance(BigDecimal.valueOf(1000L * i))
                    .currency("USD")
                    .status(status)
                    .nickname(i == 1 ? "Primary " + accountTypeLabel : null)
                    .build());
        }
        return accounts;
    }
}
```