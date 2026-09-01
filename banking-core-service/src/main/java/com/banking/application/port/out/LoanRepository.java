package com.banking.application.port.out;

import com.banking.domain.model.Loan;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port — persistence operations for Loan.
 */
public interface LoanRepository {

    Loan save(Loan loan);

    Optional<Loan> findByExternalId(String externalId);

    List<Loan> findByCustomerId(Long customerId);
}
