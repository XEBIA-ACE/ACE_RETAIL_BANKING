package com.banking.application.port.out;

import com.banking.domain.model.Payee;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port: persistence contract for Payee entities.
 * Implementations live in the adapter/out/persistence layer.
 */
public interface PayeeRepository {

    /**
     * Persist a new payee or update an existing one.
     * Generates externalId and sets createdAt/updatedAt when not already present.
     *
     * @param payee the payee to save
     * @return the saved payee with all generated fields populated
     */
    Payee save(Payee payee);

    /**
     * Find a payee by its public external identifier.
     *
     * @param externalId UUID string
     * @return an Optional containing the payee, or empty if not found
     */
    Optional<Payee> findByExternalId(String externalId);

    /**
     * Return all payees belonging to the given internal customer ID.
     *
     * @param customerId internal PK of the customer
     * @return list of payees (may be empty)
     */
    List<Payee> findAllByCustomerId(Long customerId);

    /**
     * Delete a payee by its public external identifier.
     *
     * @param externalId UUID string
     */
    void deleteByExternalId(String externalId);

    /**
     * Check whether a payee with the given account number already exists
     * for the specified customer (used for duplicate prevention).
     *
     * @param customerId    internal PK of the customer
     * @param accountNumber destination account number
     * @return true if a duplicate exists
     */
    boolean existsByCustomerIdAndAccountNumber(Long customerId, String accountNumber);
}
