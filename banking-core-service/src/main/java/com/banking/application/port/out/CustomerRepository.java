package com.banking.application.port.out;

import com.banking.domain.model.Customer;

import java.util.Optional;

/**
 * Output port for Customer persistence operations.
 * Implemented by the persistence adapter layer.
 */
public interface CustomerRepository {

    /**
     * Find a customer by their external (public) identifier.
     *
     * @param externalId the external UUID-style identifier
     * @return an Optional containing the Customer if found, or empty if not
     */
    Optional<Customer> findByExternalId(String externalId);

    /**
     * Persist a new or updated Customer.
     *
     * @param customer the customer to save
     * @return the saved Customer (may include generated id / timestamps)
     */
    Customer save(Customer customer);
}
