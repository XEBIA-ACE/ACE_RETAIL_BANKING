```java
package com.bank.core.account.service;

import com.bank.core.account.domain.Account;
import com.bank.core.account.domain.AccountStatus;
import com.bank.core.account.domain.AccountType;
import com.bank.core.account.dto.AccountSummaryDto;
import com.bank.core.account.dto.DashboardResponseDto;
import com.bank.core.account.mapper.AccountMapper;
import com.bank.core.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    private static final String CUSTOMER_EXTERNAL_ID = "ext-uuid-123";

    // -------------------------------------------------------------------------
    // Helper builders
    // -------------------------------------------------------------------------

    private Account buildAccount(String externalId, AccountType type, AccountStatus status) {
        return Account.builder()
                .externalId(externalId)
                .accountType(type)
                .accountNumber("****1234")
                .balance(BigDecimal.valueOf(1000.00))
                .currency("USD")
                .status(status)
                .nickname(null)
                .build();
    }

    private AccountSummaryDto buildSummaryDto(String externalId, AccountType type, AccountStatus status) {
        return AccountSummaryDto.builder()
                .externalId(externalId)
                .accountType(type)
                .accountNumber("****1234")
                .balance(BigDecimal.valueOf(1000.00))
                .currency("USD")
                .status(status)
                .nickname(null)
                .build();
    }

    // -------------------------------------------------------------------------
    // AC-1 / FR-01 — All three account types present
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("AC-1 / FR-01: Customer with checking, savings, and credit card accounts — all three types appear as keys in DashboardResponseDto.accounts")
    void givenCustomerWithAllThreeAccountTypes_whenGetDashboard_thenAllThreeTypeKeysPresent() {
        Account checking = buildAccount("acc-001", AccountType.CHECKING, AccountStatus.ACTIVE);
        Account savings = buildAccount("acc-002", AccountType.SAVINGS, AccountStatus.ACTIVE);
        Account creditCard = buildAccount("acc-003", AccountType.CREDIT_CARD, AccountStatus.ACTIVE);

        AccountSummaryDto checkingDto = buildSummaryDto("acc-001", AccountType.CHECKING, AccountStatus.ACTIVE);
        AccountSummaryDto savingsDto = buildSummaryDto("acc-002", AccountType.SAVINGS, AccountStatus.ACTIVE);
        AccountSummaryDto creditCardDto = buildSummaryDto("acc-003", AccountType.CREDIT_CARD, AccountStatus.ACTIVE);

        when(accountRepository.findAllByCustomerExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(List.of(checking, savings, creditCard));
        when(accountMapper.toDto(checking)).thenReturn(checkingDto);
        when(accountMapper.toDto(savings)).thenReturn(savingsDto);
        when(accountMapper.toDto(creditCard)).thenReturn(creditCardDto);

        DashboardResponseDto response = accountService.getAccountsByCustomer(CUSTOMER_EXTERNAL_ID);

        assertThat(response).isNotNull();
        assertThat(response.getAccounts())
                .containsKey(AccountType.CHECKING)
                .containsKey(AccountType.SAVINGS)
                .containsKey(AccountType.CREDIT_CARD);
    }

    // -------------------------------------------------------------------------
    // AC-2 / FR-04 — Only checking accounts; SAVINGS and CREDIT_CARD absent
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("AC-2 / FR-04: Customer with only checking accounts — SAVINGS and CREDIT_CARD keys are absent from the response map")
    void givenCustomerWithOnlyCheckingAccounts_whenGetDashboard_thenSavingsAndCreditCardKeysAbsent() {
        Account checking = buildAccount("acc-001", AccountType.CHECKING, AccountStatus.ACTIVE);
        AccountSummaryDto checkingDto = buildSummaryDto("acc-001", AccountType.CHECKING, AccountStatus.ACTIVE);

        when(accountRepository.findAllByCustomerExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(List.of(checking));
        when(accountMapper.toDto(checking)).thenReturn(checkingDto);

        DashboardResponseDto response = accountService.getAccountsByCustomer(CUSTOMER_EXTERNAL_ID);

        assertThat(response.getAccounts())
                .containsKey(AccountType.CHECKING)
                .doesNotContainKey(AccountType.SAVINGS)
                .doesNotContainKey(AccountType.CREDIT_CARD);
    }

    // -------------------------------------------------------------------------
    // AC-3 / FR-06 — Mix of ACTIVE and INACTIVE accounts; all present, INACTIVE has correct status
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("AC-3 / FR-06: Customer with ACTIVE and INACTIVE accounts — all accounts present; INACTIVE account has status = INACTIVE")
    void givenCustomerWithActiveAndInactiveAccounts_whenGetDashboard_thenAllAccountsPresentAndInactiveStatusPreserved() {
        Account activeChecking = buildAccount("acc-001", AccountType.CHECKING, AccountStatus.ACTIVE);
        Account inactiveCreditCard = buildAccount("acc-002", AccountType.CREDIT_CARD, AccountStatus.INACTIVE);

        AccountSummaryDto activeCheckingDto = buildSummaryDto("acc-001", AccountType.CHECKING, AccountStatus.ACTIVE);
        AccountSummaryDto inactiveCreditCardDto = buildSummaryDto("acc-002", AccountType.CREDIT_CARD, AccountStatus.INACTIVE);

        when(accountRepository.findAllByCustomerExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(List.of(activeChecking, inactiveCreditCard));
        when(accountMapper.toDto(activeChecking)).thenReturn(activeCheckingDto);
        when(accountMapper.toDto(inactiveCreditCard)).thenReturn(inactiveCreditCardDto);

        DashboardResponseDto response = accountService.getAccountsByCustomer(CUSTOMER_EXTERNAL_ID);

        Map<AccountType, List<AccountSummaryDto>> accounts = response.getAccounts();

        assertThat(accounts).containsKey(AccountType.CHECKING);
        assertThat(accounts).containsKey(AccountType.CREDIT_CARD);

        assertThat(accounts.get(AccountType.CHECKING))
                .hasSize(1)
                .first()
                .extracting(AccountSummaryDto::getStatus)
                .isEqualTo(AccountStatus.ACTIVE);

        assertThat(accounts.get(AccountType.CREDIT_CARD))
                .hasSize(1)
                .first()
                .extracting(AccountSummaryDto::getStatus)
                .isEqualTo(AccountStatus.INACTIVE);
    }

    // -------------------------------------------------------------------------
    // AC-4 — Single account type; only that key present
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("AC-4: Customer with a single account type (SAVINGS) — only SAVINGS key is present in the response map")
    void givenCustomerWithOnlySavingsAccount_whenGetDashboard_thenOnlySavingsKeyPresent() {
        Account savings = buildAccount("acc-001", AccountType.SAVINGS, AccountStatus.ACTIVE);
        AccountSummaryDto savingsDto = buildSummaryDto("acc-001", AccountType.SAVINGS, AccountStatus.ACTIVE);

        when(accountRepository.findAllByCustomerExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(List.of(savings));
        when(accountMapper.toDto(savings)).thenReturn(savingsDto);

        DashboardResponseDto response = accountService.getAccountsByCustomer(CUSTOMER_EXTERNAL_ID);

        assertThat(response.getAccounts())
                .hasSize(1)
                .containsKey(AccountType.SAVINGS)
                .doesNotContainKey(AccountType.CHECKING)
                .doesNotContainKey(AccountType.CREDIT_CARD);
    }

    // -------------------------------------------------------------------------
    // FR-03 — Service passes the correct customerExternalId to the repository
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("FR-03: Service passes the correct customerExternalId to the repository (verify mock interaction)")
    void givenCustomerExternalId_whenGetDashboard_thenRepositoryCalledWithCorrectExternalId() {
        when(accountRepository.findAllByCustomerExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(List.of());

        accountService.getAccountsByCustomer(CUSTOMER_EXTERNAL_ID);

        verify(accountRepository).findAllByCustomerExternalId(CUSTOMER_EXTERNAL_ID);
    }

    // -------------------------------------------------------------------------
    // Grouping correctness — Multiple accounts of the same type grouped under same key
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Grouping correctness: Multiple accounts of the same type are grouped under the same key")
    void givenMultipleAccountsOfSameType_whenGetDashboard_thenGroupedUnderSingleKey() {
        Account checking1 = buildAccount("acc-001", AccountType.CHECKING, AccountStatus.ACTIVE);
        Account checking2 = buildAccount("acc-002", AccountType.CHECKING, AccountStatus.ACTIVE);
        Account checking3 = buildAccount("acc-003", AccountType.CHECKING, AccountStatus.ACTIVE);

        AccountSummaryDto checkingDto1 = buildSummaryDto("acc-001", AccountType.CHECKING, AccountStatus.ACTIVE);
        AccountSummaryDto checkingDto2 = buildSummaryDto("acc-002", AccountType.CHECKING, AccountStatus.ACTIVE);
        AccountSummaryDto checkingDto3 = buildSummaryDto("acc-003", AccountType.CHECKING, AccountStatus.ACTIVE);

        when(accountRepository.findAllByCustomerExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(List.of(checking1, checking2, checking3));
        when(accountMapper.toDto(checking1)).thenReturn(checkingDto1);
        when(accountMapper.toDto(checking2)).thenReturn(checkingDto2);
        when(accountMapper.toDto(checking3)).thenReturn(checkingDto3);

        DashboardResponseDto response = accountService.getAccountsByCustomer(CUSTOMER_EXTERNAL_ID);

        assertThat(response.getAccounts())
                .hasSize(1)
                .containsKey(AccountType.CHECKING);

        assertThat(response.getAccounts().get(AccountType.CHECKING))
                .hasSize(3)
                .extracting(AccountSummaryDto::getExternalId)
                .containsExactlyInAnyOrder("acc-001", "acc-002", "acc-003");
    }

    // -------------------------------------------------------------------------
    // Empty result — Repository returns empty list; accounts map is empty (not null)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Empty result: Repository returns empty list — DashboardResponseDto.accounts is an empty map, not null")
    void givenRepositoryReturnsEmptyList_whenGetDashboard_thenAccountsMapIsEmptyNotNull() {
        when(accountRepository.findAllByCustomerExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(List.of());

        DashboardResponseDto response = accountService.getAccountsByCustomer(CUSTOMER_EXTERNAL_ID);

        assertThat(response).isNotNull();
        assertThat(response.getAccounts())
                .isNotNull()
                .isEmpty();
    }
}
```