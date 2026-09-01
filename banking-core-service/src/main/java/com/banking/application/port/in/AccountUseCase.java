package com.banking.application.port.in;

import com.banking.domain.model.Account;
import com.banking.domain.model.AccountType;

import java.util.List;
import java.util.Optional;

/**
 * Inbound port — use cases for account management.
 */
public interface AccountUseCase {

    Account openAccount(OpenAccountCommand command);

    Optional<Account> findAccountById(String externalId);

    List<Account> listAccountsByCustomer(String customerExternalId);

    Account closeAccount(String externalId);

    record OpenAccountCommand(
            String customerExternalId,
            AccountType accountType,
            String currency
    ) {}
}
