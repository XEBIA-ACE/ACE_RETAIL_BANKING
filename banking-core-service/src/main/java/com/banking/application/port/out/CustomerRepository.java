package com.banking.application.port.out;

import com.banking.domain.model.Customer;

import java.util.Optional;

/**
 * Outbound port for customer persistence operations.
 */
public interface CustomerRepository {

    /**
     * Finds a customer by their public external identifier.
     *
     * @param externalId public customer identifier
     * @return {@link Optional} containing the customer, or empty if not found
     */
    Optional<Customer> findByExternalId(String externalId);
}
