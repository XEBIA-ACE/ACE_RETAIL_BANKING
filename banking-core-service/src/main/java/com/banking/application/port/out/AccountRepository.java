package com.banking.application.port.out;

import com.banking.domain.model.Account;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port — persistence operations for Account.
 */
public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(Long id);

    Optional<Account> findByExternalId(String externalId);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);

    boolean existsByAccountNumber(String accountNumber);
}
