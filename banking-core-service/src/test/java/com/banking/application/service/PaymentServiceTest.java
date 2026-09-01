package com.banking.application.service;

import com.banking.application.port.in.PaymentUseCase;
import com.banking.application.port.out.AccountRepository;
import com.banking.application.port.out.PaymentRepository;
import com.banking.domain.exception.BusinessRuleException;
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
@DisplayName("PaymentService")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("initiatePayment — creates PENDING payment")
    void initiatePayment_createsPendingPayment() {
        Account source = buildAccount(1L, "src-001", new BigDecimal("1000.00"));
        Account target = buildAccount(2L, "tgt-001", new BigDecimal("200.00"));
        when(accountRepository.findByExternalId("src-001")).thenReturn(Optional.of(source));
        when(accountRepository.findByExternalId("tgt-001")).thenReturn(Optional.of(target));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            return p.toBuilder().id(1L).build();
        });

        PaymentUseCase.InitiatePaymentCommand cmd = new PaymentUseCase.InitiatePaymentCommand(
                "src-001", "tgt-001", new BigDecimal("300.00"),
                "USD", PaymentType.INTERNAL_TRANSFER, "Test transfer"
        );

        Payment result = paymentService.initiatePayment(cmd);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    @DisplayName("initiatePayment — throws InsufficientFundsException when source balance too low")
    void initiatePayment_throwsWhenInsufficientFunds() {
        Account source = buildAccount(1L, "src-001", new BigDecimal("50.00"));
        Account target = buildAccount(2L, "tgt-001", new BigDecimal("200.00"));
        when(accountRepository.findByExternalId("src-001")).thenReturn(Optional.of(source));
        when(accountRepository.findByExternalId("tgt-001")).thenReturn(Optional.of(target));

        PaymentUseCase.InitiatePaymentCommand cmd = new PaymentUseCase.InitiatePaymentCommand(
                "src-001", "tgt-001", new BigDecimal("300.00"),
                "USD", PaymentType.INTERNAL_TRANSFER, "Test transfer"
        );

        assertThatThrownBy(() -> paymentService.initiatePayment(cmd))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    @DisplayName("processPayment — transitions PENDING → COMPLETED and updates balances")
    void processPayment_completesAndUpdatesBalances() {
        Account source = buildAccount(1L, "src-001", new BigDecimal("1000.00"));
        Account target = buildAccount(2L, "tgt-001", new BigDecimal("200.00"));
        Payment pending = buildPayment(PaymentStatus.PENDING, source.getId(), target.getId());

        when(paymentRepository.findByExternalId("pay-001")).thenReturn(Optional.of(pending));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(target));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.processPayment("pay-001");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("processPayment — throws BusinessRuleException when payment is not PENDING")
    void processPayment_throwsWhenNotPending() {
        Payment completed = buildPayment(PaymentStatus.COMPLETED, 1L, 2L);
        when(paymentRepository.findByExternalId("pay-001")).thenReturn(Optional.of(completed));

        assertThatThrownBy(() -> paymentService.processPayment("pay-001"))
                .isInstanceOf(BusinessRuleException.class);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Account buildAccount(Long id, String externalId, BigDecimal balance) {
        return Account.builder()
                .id(id).externalId(externalId)
                .customerId(1L).accountNumber("ACC" + id)
                .accountType(AccountType.CHECKING)
                .balance(balance).currency("USD")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    private Payment buildPayment(PaymentStatus status, Long sourceId, Long targetId) {
        return Payment.builder()
                .id(1L).externalId("pay-001")
                .sourceAccount(sourceId).targetAccount(targetId)
                .amount(new BigDecimal("300.00")).currency("USD")
                .paymentType(PaymentType.INTERNAL_TRANSFER)
                .description("Test")
                .status(status)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }
}
