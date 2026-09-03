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
 * Unit tests for {@link AccountService}, focusing on the {@code getAccountBalances} method.
 * No Spring context is loaded — Mockito only.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private static final String CUSTOMER_EXTERNAL_ID = "cust-ext-001";
    private static final Long CUSTOMER_ID = 1L;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(CUSTOMER_ID)
                .externalId(CUSTOMER_EXTERNAL_ID)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("+1-555-0100")
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // getAccountBalances — single account happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAccountBalances — single account returns correct balance and currency")
    void getAccountBalances_singleAccount_returnsCorrectBalanceAndCurrency() {
        // Given
        Account checkingAccount = Account.builder()
                .id(10L)
                .externalId("acc-ext-001")
                .customerId(CUSTOMER_ID)
                .accountNumber("0001234567")
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("1500.75"))
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(customerRepository.findByExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomerId(CUSTOMER_ID))
                .thenReturn(List.of(checkingAccount));

        // When
        List<Account> result = accountService.getAccountBalances(CUSTOMER_EXTERNAL_ID);

        // Then
        assertThat(result).hasSize(1);
        Account returned = result.get(0);
        assertThat(returned.getBalance()).isEqualByComparingTo(new BigDecimal("1500.75"));
        assertThat(returned.getCurrency()).isEqualTo("USD");
        assertThat(returned.getAccountType()).isEqualTo(AccountType.CHECKING);
    }

    // -------------------------------------------------------------------------
    // getAccountBalances — multiple accounts happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAccountBalances — multiple accounts each return their own balance and currency")
    void getAccountBalances_multipleAccounts_returnsDistinctBalancesAndCurrencies() {
        // Given
        Account checkingAccount = Account.builder()
                .id(10L)
                .externalId("acc-ext-001")
                .customerId(CUSTOMER_ID)
                .accountNumber("0001234567")
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("2500.00"))
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Account savingsAccount = Account.builder()
                .id(11L)
                .externalId("acc-ext-002")
                .customerId(CUSTOMER_ID)
                .accountNumber("0009876543")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("8750.50"))
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(customerRepository.findByExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomerId(CUSTOMER_ID))
                .thenReturn(List.of(checkingAccount, savingsAccount));

        // When
        List<Account> result = accountService.getAccountBalances(CUSTOMER_EXTERNAL_ID);

        // Then
        assertThat(result).hasSize(2);

        Account checking = result.stream()
                .filter(a -> a.getAccountType() == AccountType.CHECKING)
                .findFirst()
                .orElseThrow();
        assertThat(checking.getBalance()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(checking.getCurrency()).isEqualTo("USD");

        Account savings = result.stream()
                .filter(a -> a.getAccountType() == AccountType.SAVINGS)
                .findFirst()
                .orElseThrow();
        assertThat(savings.getBalance()).isEqualByComparingTo(new BigDecimal("8750.50"));
        assertThat(savings.getCurrency()).isEqualTo("USD");

        // Balances must be distinct
        assertThat(checking.getBalance()).isNotEqualByComparingTo(savings.getBalance());
    }

    // -------------------------------------------------------------------------
    // getAccountBalances — unknown customer throws ResourceNotFoundException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAccountBalances — unknown customerExternalId throws ResourceNotFoundException")
    void getAccountBalances_unknownCustomer_throwsResourceNotFoundException() {
        // Given
        String unknownId = "cust-does-not-exist";
        when(customerRepository.findByExternalId(unknownId))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> accountService.getAccountBalances(unknownId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(unknownId);
    }
}
