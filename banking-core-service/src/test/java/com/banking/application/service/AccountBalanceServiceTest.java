package com.banking.application.service;

import com.banking.application.port.out.AccountRepository;
import com.banking.application.port.out.CustomerRepository;
import com.banking.domain.exception.ResourceNotFoundException;
import com.banking.domain.model.Account;
import com.banking.domain.model.AccountStatus;
import com.banking.domain.model.AccountType;
import com.banking.domain.model.Customer;
import com.banking.domain.model.CustomerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Dedicated unit tests for the {@code getAccountBalances} method of {@link AccountService}.
 *
 * <p>This class mirrors the scenarios in {@link AccountServiceTest} as standalone tests,
 * satisfying the new-file requirement from the implementation plan (plan.md).
 *
 * <p>No Spring context is loaded — Mockito only ({@code @ExtendWith(MockitoExtension.class)}).
 */
@ExtendWith(MockitoExtension.class)
class AccountBalanceServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private static final String CUSTOMER_EXTERNAL_ID = "cust-balance-001";
    private static final Long CUSTOMER_ID = 42L;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(CUSTOMER_ID)
                .externalId(CUSTOMER_EXTERNAL_ID)
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("+1-555-0200")
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // Scenario 1: Single account — happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAccountBalances — single account: balance and currency are correct")
    void getAccountBalances_singleAccount_balanceAndCurrencyAreCorrect() {
        // Given — one CHECKING account with a known balance
        Account account = Account.builder()
                .id(100L)
                .externalId("bal-acc-ext-001")
                .customerId(CUSTOMER_ID)
                .accountNumber("1110001111")
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("3200.00"))
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(customerRepository.findByExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomerId(CUSTOMER_ID))
                .thenReturn(List.of(account));

        // When
        List<Account> balances = accountService.getAccountBalances(CUSTOMER_EXTERNAL_ID);

        // Then — exactly one account returned with the expected balance and currency
        assertThat(balances).hasSize(1);
        Account result = balances.get(0);
        assertThat(result.getBalance())
                .as("balance should be 3200.00")
                .isEqualByComparingTo(new BigDecimal("3200.00"));
        assertThat(result.getCurrency())
                .as("currency should be USD")
                .isEqualTo("USD");
    }

    // -------------------------------------------------------------------------
    // Scenario 2: Multiple accounts — happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAccountBalances — two accounts: each has its own distinct balance and currency")
    void getAccountBalances_twoAccounts_eachHasDistinctBalanceAndCurrency() {
        // Given — CHECKING and SAVINGS accounts with different balances
        Account checking = Account.builder()
                .id(101L)
                .externalId("bal-acc-ext-002")
                .customerId(CUSTOMER_ID)
                .accountNumber("2220002222")
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Account savings = Account.builder()
                .id(102L)
                .externalId("bal-acc-ext-003")
                .customerId(CUSTOMER_ID)
                .accountNumber("3330003333")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("15000.99"))
                .currency("EUR")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(customerRepository.findByExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomerId(CUSTOMER_ID))
                .thenReturn(List.of(checking, savings));

        // When
        List<Account> balances = accountService.getAccountBalances(CUSTOMER_EXTERNAL_ID);

        // Then — two accounts, each with its own balance and currency
        assertThat(balances).hasSize(2);

        Account checkingResult = balances.stream()
                .filter(a -> a.getAccountType() == AccountType.CHECKING)
                .findFirst()
                .orElseThrow(() -> new AssertionError("CHECKING account not found in result"));
        assertThat(checkingResult.getBalance())
                .as("CHECKING balance should be 1000.00")
                .isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(checkingResult.getCurrency())
                .as("CHECKING currency should be USD")
                .isEqualTo("USD");

        Account savingsResult = balances.stream()
                .filter(a -> a.getAccountType() == AccountType.SAVINGS)
                .findFirst()
                .orElseThrow(() -> new AssertionError("SAVINGS account not found in result"));
        assertThat(savingsResult.getBalance())
                .as("SAVINGS balance should be 15000.99")
                .isEqualByComparingTo(new BigDecimal("15000.99"));
        assertThat(savingsResult.getCurrency())
                .as("SAVINGS currency should be EUR")
                .isEqualTo("EUR");

        // Balances must differ from each other
        assertThat(checkingResult.getBalance())
                .isNotEqualByComparingTo(savingsResult.getBalance());
    }

    // -------------------------------------------------------------------------
    // Scenario 3: Unknown customer — ResourceNotFoundException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAccountBalances — unknown customer throws ResourceNotFoundException")
    void getAccountBalances_unknownCustomer_throwsResourceNotFoundException() {
        // Given — no customer exists for this external ID
        String unknownExternalId = "cust-unknown-xyz";
        when(customerRepository.findByExternalId(unknownExternalId))
                .thenReturn(Optional.empty());

        // When / Then — ResourceNotFoundException must be thrown and message must reference the ID
        assertThatThrownBy(() -> accountService.getAccountBalances(unknownExternalId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(unknownExternalId);
    }
}
