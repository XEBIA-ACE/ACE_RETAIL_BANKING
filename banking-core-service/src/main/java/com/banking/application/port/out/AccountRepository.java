package com.banking.application.port.out;

import com.banking.domain.model.Account;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for account persistence operations.
 */
public interface AccountRepository {

    /**
     * Finds all accounts whose internal customer FK matches the given customer id.
     *
     * @param customerId internal customer primary key
     * @return list of matching {@link Account} domain objects; empty list if none
     */
    List<Account> findByCustomerId(Long customerId);

    /**
     * Finds a single account by its external identifier.
     *
     * @param externalId public account identifier
     * @return {@link Optional} containing the account, or empty if not found
     */
    Optional<Account> findByExternalId(String externalId);
}
