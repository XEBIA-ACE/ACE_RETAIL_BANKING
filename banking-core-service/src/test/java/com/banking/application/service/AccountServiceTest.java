package com.banking.application.service;

import com.banking.application.port.in.AccountUseCase;
import com.banking.application.port.out.AccountRepository;
import com.banking.application.port.out.CustomerRepository;
import com.banking.domain.exception.BusinessRuleException;
import com.banking.domain.exception.ResourceNotFoundException;
import com.banking.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("openAccount — creates ACTIVE account with zero balance")
    void openAccount_createsActiveAccountWithZeroBalance() {
        Customer customer = buildCustomer();
        when(customerRepository.findByExternalId("cust-001")).thenReturn(Optional.of(customer));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            return a.toBuilder().id(1L).build();
        });

        AccountUseCase.OpenAccountCommand cmd = new AccountUseCase.OpenAccountCommand(
                "cust-001", AccountType.CHECKING, "USD"
        );
        Account result = accountService.openAccount(cmd);

        assertThat(result.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getAccountType()).isEqualTo(AccountType.CHECKING);
    }

    @Test
    @DisplayName("openAccount — throws ResourceNotFoundException when customer not found")
    void openAccount_throwsWhenCustomerNotFound() {
        when(customerRepository.findByExternalId("unknown")).thenReturn(Optional.empty());

        AccountUseCase.OpenAccountCommand cmd = new AccountUseCase.OpenAccountCommand(
                "unknown", AccountType.SAVINGS, "USD"
        );

        assertThatThrownBy(() -> accountService.openAccount(cmd))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("closeAccount — throws BusinessRuleException when balance is non-zero")
    void closeAccount_throwsWhenBalanceNonZero() {
        Account account = buildAccount(new BigDecimal("100.00"));
        when(accountRepository.findByExternalId("acc-001")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.closeAccount("acc-001"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("non-zero balance");
    }

    @Test
    @DisplayName("closeAccount — sets status to CLOSED when balance is zero")
    void closeAccount_setsStatusClosedWhenBalanceZero() {
        Account account = buildAccount(BigDecimal.ZERO);
        when(accountRepository.findByExternalId("acc-001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.closeAccount("acc-001");

        assertThat(result.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Customer buildCustomer() {
        return Customer.builder()
                .id(1L).externalId("cust-001")
                .firstName("John").lastName("Smith")
                .email("john@example.com").phone("+1-555-0200")
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    private Account buildAccount(BigDecimal balance) {
        return Account.builder()
                .id(1L).externalId("acc-001")
                .customerId(1L).accountNumber("ACC123456")
                .accountType(AccountType.CHECKING)
                .balance(balance).currency("USD")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }
}
