package com.banking.application.port.in;

import com.banking.domain.model.Account;

import java.util.List;

/**
 * Inbound port for account-related use cases.
 */
public interface AccountUseCase {

    /**
     * Returns all accounts (with live balances) for the given customer.
     *
     * @param customerExternalId public customer identifier
     * @return list of {@link Account} domain objects; never {@code null}
     * @throws com.banking.domain.exception.ResourceNotFoundException if the customer does not exist
     */
    List<Account> getAccountBalances(String customerExternalId);

    /**
     * Lists all accounts belonging to a customer.
     *
     * @param customerExternalId public customer identifier
     * @return list of {@link Account} domain objects
     */
    List<Account> listAccountsByCustomer(String customerExternalId);
}
