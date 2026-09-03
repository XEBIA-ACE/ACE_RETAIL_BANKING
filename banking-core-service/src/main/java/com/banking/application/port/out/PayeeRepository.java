package com.banking.application.port.out;

import com.banking.domain.model.Payee;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for Payee persistence operations.
 */
public interface PayeeRepository {

    Payee save(Payee payee);

    Optional<Payee> findByExternalId(String externalId);

    List<Payee> findByCustomerId(Long customerId);

    void deleteByExternalId(String externalId);

    boolean existsByCustomerIdAndAccountNumber(Long customerId, String accountNumber);
}