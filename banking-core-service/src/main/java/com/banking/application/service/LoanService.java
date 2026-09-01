package com.banking.application.service;

import com.banking.application.port.in.LoanUseCase;
import com.banking.application.port.out.CustomerRepository;
import com.banking.application.port.out.LoanRepository;
import com.banking.domain.exception.BusinessRuleException;
import com.banking.domain.exception.ResourceNotFoundException;
import com.banking.domain.model.Customer;
import com.banking.domain.model.Loan;
import com.banking.domain.model.LoanStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LoanService implements LoanUseCase {

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;

    @Override
    public Loan applyForLoan(ApplyForLoanCommand command) {
        Customer customer = customerRepository.findByExternalId(command.customerExternalId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", command.customerExternalId()));

        BigDecimal monthlyRate = command.interestRate().divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal monthlyPayment = calculateMonthlyPayment(command.principal(), monthlyRate, command.termMonths());

        Loan loan = Loan.builder()
                .externalId(UUID.randomUUID().toString())
                .customerId(customer.getId())
                .principal(command.principal())
                .interestRate(command.interestRate())
                .termMonths(command.termMonths())
                .monthlyPayment(monthlyPayment)
                .outstanding(command.principal())
                .currency(command.currency())
                .status(LoanStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Loan saved = loanRepository.save(loan);
        log.info("Loan application submitted: {}", saved.getExternalId());
        return saved;
    }

    @Override
    public Loan approveLoan(String externalId) {
        Loan loan = loanRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", externalId));
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BusinessRuleException("Loan is not in PENDING status: " + externalId);
        }
        return loanRepository.save(loan.toBuilder().status(LoanStatus.APPROVED).updatedAt(LocalDateTime.now()).build());
    }

    @Override
    public Loan disburseLoan(String externalId) {
        Loan loan = loanRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", externalId));
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new BusinessRuleException("Loan is not in APPROVED status: " + externalId);
        }
        return loanRepository.save(loan.toBuilder()
                .status(LoanStatus.DISBURSED)
                .disbursedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Loan> findLoanById(String externalId) {
        return loanRepository.findByExternalId(externalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> listLoansByCustomer(String customerExternalId) {
        Customer customer = customerRepository.findByExternalId(customerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerExternalId));
        return loanRepository.findByCustomerId(customer.getId());
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal monthlyRate, int termMonths) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }
        // M = P * [r(1+r)^n] / [(1+r)^n - 1]
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal pow = onePlusR.pow(termMonths);
        return principal.multiply(monthlyRate).multiply(pow)
                .divide(pow.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
    }
}
