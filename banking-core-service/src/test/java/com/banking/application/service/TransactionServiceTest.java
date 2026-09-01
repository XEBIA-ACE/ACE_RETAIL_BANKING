package com.banking.application.service;

import com.banking.application.port.in.TransactionUseCase;
import com.banking.application.port.out.AccountRepository;
import com.banking.application.port.out.TransactionRepository;
import com.banking.domain.exception.InsufficientFundsException;
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
@DisplayName("TransactionService")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("recordTransaction — CREDIT increases account balance")
    void recordTransaction_creditIncreasesBalance() {
        Account account = buildAccount(new BigDecimal("500.00"));
        when(accountRepository.findByExternalId("acc-001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            return t.toBuilder().id(1L).build();
        });

        TransactionUseCase.RecordTransactionCommand cmd = new TransactionUseCase.RecordTransactionCommand(
                "acc-001", TransactionType.CREDIT, new BigDecimal("200.00"),
                "USD", "Deposit", "REF001"
        );

        Transaction result = transactionService.recordTransaction(cmd);

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(result.getTransactionType()).isEqualTo(TransactionType.CREDIT);
        // Verify balance was updated to 700
        verify(accountRepository).save(argThat(a ->
                a.getBalance().compareTo(new BigDecimal("700.00")) == 0));
    }

    @Test
    @DisplayName("recordTransaction — DEBIT throws InsufficientFundsException when balance too low")
    void recordTransaction_debitThrowsWhenInsufficientFunds() {
        Account account = buildAccount(new BigDecimal("50.00"));
        when(accountRepository.findByExternalId("acc-001")).thenReturn(Optional.of(account));

        TransactionUseCase.RecordTransactionCommand cmd = new TransactionUseCase.RecordTransactionCommand(
                "acc-001", TransactionType.DEBIT, new BigDecimal("200.00"),
                "USD", "Withdrawal", "REF002"
        );

        assertThatThrownBy(() -> transactionService.recordTransaction(cmd))
                .isInstanceOf(InsufficientFundsException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("recordTransaction — DEBIT decreases account balance")
    void recordTransaction_debitDecreasesBalance() {
        Account account = buildAccount(new BigDecimal("500.00"));
        when(accountRepository.findByExternalId("acc-001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            return t.toBuilder().id(1L).build();
        });

        TransactionUseCase.RecordTransactionCommand cmd = new TransactionUseCase.RecordTransactionCommand(
                "acc-001", TransactionType.DEBIT, new BigDecimal("100.00"),
                "USD", "Withdrawal", "REF003"
        );

        transactionService.recordTransaction(cmd);

        verify(accountRepository).save(argThat(a ->
                a.getBalance().compareTo(new BigDecimal("400.00")) == 0));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

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
