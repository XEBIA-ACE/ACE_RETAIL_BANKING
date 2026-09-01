package com.banking.application.port.out;

import com.banking.domain.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port — persistence operations for Customer.
 */
public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findByExternalId(String externalId);

    Optional<Customer> findByEmail(String email);

    List<Customer> findAll(int page, int size);

    boolean existsByEmail(String email);
}
