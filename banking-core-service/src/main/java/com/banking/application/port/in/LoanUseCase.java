package com.banking.application.port.in;

import com.banking.domain.model.Loan;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Inbound port — use cases for loan processing.
 */
public interface LoanUseCase {

    Loan applyForLoan(ApplyForLoanCommand command);

    Loan approveLoan(String externalId);

    Loan disburseLoan(String externalId);

    Optional<Loan> findLoanById(String externalId);

    List<Loan> listLoansByCustomer(String customerExternalId);

    record ApplyForLoanCommand(
            String customerExternalId,
            BigDecimal principal,
            BigDecimal interestRate,
            Integer termMonths,
            String currency
    ) {}
}
