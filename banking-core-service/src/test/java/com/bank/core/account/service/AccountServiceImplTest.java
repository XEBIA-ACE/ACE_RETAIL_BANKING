```java
package com.bank.core.account.service;

import com.bank.core.account.domain.Account;
import com.bank.core.account.domain.AccountType;
import com.bank.core.account.dto.AccountSummaryDto;
import com.bank.core.account.dto.DashboardResponse;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountServiceImpl Unit Tests")
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account checkingAccount;
    private Account savingsAccount;
    private AccountSummaryDto checkingDto;
    private AccountSummaryDto savingsDto;

    @BeforeEach
    void setUp() {
        checkingAccount = buildAccount(1L, AccountType.CHECKING, "1234567890001234");
        savingsAccount = buildAccount(2L, AccountType.SAVINGS, "9876543210008765");

        checkingDto = buildAccountSummaryDto(
                checkingAccount.getExternalId(),
                "Checking Account",
                "****1234",
                new BigDecimal("1250.00"),
                "USD",
                "Checking Account ending in 1234"
        );

        savingsDto = buildAccountSummaryDto(
                savingsAccount.getExternalId(),
                "Savings Account",
                "****8765",
                new BigDecimal("5400.50"),
                "USD",
                "Savings Account ending in 8765"
        );
    }

    // -----------------------------------------------------------------------
    // Scenario 1: Happy path — multiple accounts (checking + savings)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getDashboard — multiple accounts — returns hasAccounts=true, null emptyStateMessage, list size 2, mapper called once")
    void getDashboard_multipleAccounts_returnsCorrectDashboardResponse() {
        // Arrange
        Long customerId = 42L;
        List<Account> accounts = List.of(checkingAccount, savingsAccount);

        when(accountRepository.findAllByCustomerId(customerId)).thenReturn(accounts);
        when(accountMapper.toDto(checkingAccount)).thenReturn(checkingDto);
        when(accountMapper.toDto(savingsAccount)).thenReturn(savingsDto);

        // Act
        DashboardResponse response = accountService.getDashboard(customerId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isHasAccounts()).isTrue();
        assertThat(response.getEmptyStateMessage()).isNull();
        assertThat(response.getAccounts()).hasSize(2);
        assertThat(response.getAccounts()).containsExactlyInAnyOrder(checkingDto, savingsDto);

        // Mapper must be invoked once per account — total 2 invocations via toDtoList delegation
        // (if mapper maps the list in a single call, verify toDto is called for each element)
        verify(accountMapper, times(1)).toDtoList(accounts);
    }

    // -----------------------------------------------------------------------
    // Scenario 2: Happy path — single account
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getDashboard — single account — returns hasAccounts=true, list size 1")
    void getDashboard_singleAccount_returnsHasAccountsTrueAndListSizeOne() {
        // Arrange
        Long customerId = 7L;
        List<Account> accounts = List.of(checkingAccount);

        when(accountRepository.findAllByCustomerId(customerId)).thenReturn(accounts);
        when(accountMapper.toDtoList(accounts)).thenReturn(List.of(checkingDto));

        // Act
        DashboardResponse response = accountService.getDashboard(customerId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isHasAccounts()).isTrue();
        assertThat(response.getAccounts()).hasSize(1);
        assertThat(response.getAccounts().get(0)).isEqualTo(checkingDto);
    }

    // -----------------------------------------------------------------------
    // Scenario 3: Empty state — no accounts linked
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getDashboard — no accounts — returns hasAccounts=false, non-null non-empty emptyStateMessage, empty list")
    void getDashboard_noAccounts_returnsEmptyState() {
        // Arrange
        Long customerId = 99L;

        when(accountRepository.findAllByCustomerId(customerId)).thenReturn(Collections.emptyList());
        when(accountMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        DashboardResponse response = accountService.getDashboard(customerId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isHasAccounts()).isFalse();
        assertThat(response.getEmptyStateMessage())
                .isNotNull()
                .isNotBlank();
        assertThat(response.getAccounts()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Scenario 4: Single query assertion — findAllByCustomerId called exactly once
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getDashboard — verifies findAllByCustomerId is called exactly once per invocation")
    void getDashboard_singleQueryPerInvocation_repositoryCalledExactlyOnce() {
        // Arrange
        Long customerId = 15L;

        when(accountRepository.findAllByCustomerId(customerId)).thenReturn(Collections.emptyList());
        when(accountMapper.toDtoList(any())).thenReturn(Collections.emptyList());

        // Act
        accountService.getDashboard(customerId);

        // Assert
        verify(accountRepository, times(1)).findAllByCustomerId(customerId);
    }

    // -----------------------------------------------------------------------
    // Scenario 5: Correct customerId propagation
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getDashboard — exact customerId passed to getDashboard is forwarded to findAllByCustomerId")
    void getDashboard_correctCustomerIdPropagation_passesExactIdToRepository() {
        // Arrange
        Long expectedCustomerId = 12345L;

        when(accountRepository.findAllByCustomerId(expectedCustomerId)).thenReturn(Collections.emptyList());
        when(accountMapper.toDtoList(any())).thenReturn(Collections.emptyList());

        // Act
        accountService.getDashboard(expectedCustomerId);

        // Assert — verify the exact customerId is forwarded, not a default/hardcoded value
        verify(accountRepository).findAllByCustomerId(eq(expectedCustomerId));
    }

    // -----------------------------------------------------------------------
    // Helper builders
    // -----------------------------------------------------------------------

    private Account buildAccount(Long id, AccountType type, String accountNumber) {
        Account account = new Account();
        account.setId(id);
        account.setExternalId(UUID.randomUUID().toString());
        account.setCustomerId(100L);
        account.setAccountType(type);
        account.setAccountNumber(accountNumber);
        account.setCurrentBalance(BigDecimal.TEN);
        account.setCurrency("USD");
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        return account;
    }

    private AccountSummaryDto buildAccountSummaryDto(
            String accountId,
            String accountTypeName,
            String maskedAccountNumber,
            BigDecimal currentBalance,
            String currencyCode,
            String ariaLabel) {
        return AccountSummaryDto.builder()
                .accountId(accountId)
                .accountTypeName(accountTypeName)
                .maskedAccountNumber(maskedAccountNumber)
                .currentBalance(currentBalance)
                .currencyCode(currencyCode)
                .ariaLabel(ariaLabel)
                .build();
    }
}
```