package com.banking.application.service;

import com.banking.application.port.in.LoanUseCase;
import com.banking.application.port.out.CustomerRepository;
import com.banking.application.port.out.LoanRepository;
import com.banking.domain.exception.BusinessRuleException;
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
@DisplayName("LoanService")
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private LoanService loanService;

    @Test
    @DisplayName("applyForLoan — creates loan in PENDING status")
    void applyForLoan_createsPendingLoan() {
        Customer customer = buildCustomer();
        when(customerRepository.findByExternalId("cust-001")).thenReturn(Optional.of(customer));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> {
            Loan l = inv.getArgument(0);
            return l.toBuilder().id(1L).build();
        });

        LoanUseCase.ApplyForLoanCommand cmd = new LoanUseCase.ApplyForLoanCommand(
                "cust-001", new BigDecimal("10000.00"),
                new BigDecimal("5.00"), 24, "USD"
        );

        Loan result = loanService.applyForLoan(cmd);

        assertThat(result.getStatus()).isEqualTo(LoanStatus.PENDING);
        assertThat(result.getPrincipal()).isEqualByComparingTo(new BigDecimal("10000.00"));
        assertThat(result.getMonthlyPayment()).isPositive();
    }

    @Test
    @DisplayName("approveLoan — transitions PENDING → APPROVED")
    void approveLoan_transitionsPendingToApproved() {
        Loan pendingLoan = buildLoan(LoanStatus.PENDING);
        when(loanRepository.findByExternalId("loan-001")).thenReturn(Optional.of(pendingLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan result = loanService.approveLoan("loan-001");

        assertThat(result.getStatus()).isEqualTo(LoanStatus.APPROVED);
    }

    @Test
    @DisplayName("approveLoan — throws BusinessRuleException when loan is not PENDING")
    void approveLoan_throwsWhenNotPending() {
        Loan approvedLoan = buildLoan(LoanStatus.APPROVED);
        when(loanRepository.findByExternalId("loan-001")).thenReturn(Optional.of(approvedLoan));

        assertThatThrownBy(() -> loanService.approveLoan("loan-001"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("disburseLoan — transitions APPROVED → DISBURSED and sets disbursedAt")
    void disburseLoan_transitionsApprovedToDisbursed() {
        Loan approvedLoan = buildLoan(LoanStatus.APPROVED);
        when(loanRepository.findByExternalId("loan-001")).thenReturn(Optional.of(approvedLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan result = loanService.disburseLoan("loan-001");

        assertThat(result.getStatus()).isEqualTo(LoanStatus.DISBURSED);
        assertThat(result.getDisbursedAt()).isNotNull();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Customer buildCustomer() {
        return Customer.builder()
                .id(1L).externalId("cust-001")
                .firstName("Alice").lastName("Smith")
                .email("alice@example.com").phone("+1-555-0300")
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    private Loan buildLoan(LoanStatus status) {
        return Loan.builder()
                .id(1L).externalId("loan-001")
                .customerId(1L)
                .principal(new BigDecimal("10000.00"))
                .interestRate(new BigDecimal("5.00"))
                .termMonths(24)
                .monthlyPayment(new BigDecimal("438.71"))
                .outstanding(new BigDecimal("10000.00"))
                .currency("USD")
                .status(status)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }
}
