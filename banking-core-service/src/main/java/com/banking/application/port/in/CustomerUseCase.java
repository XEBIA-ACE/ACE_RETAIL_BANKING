package com.banking.application.port.in;

import com.banking.domain.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Inbound port — use cases for customer management.
 */
public interface CustomerUseCase {

    Customer onboardCustomer(OnboardCustomerCommand command);

    Optional<Customer> findCustomerById(String externalId);

    List<Customer> listCustomers(int page, int size);

    Customer updateCustomer(String externalId, UpdateCustomerCommand command);

    void deactivateCustomer(String externalId);

    record OnboardCustomerCommand(
            String firstName,
            String lastName,
            String email,
            String phone
    ) {}

    record UpdateCustomerCommand(
            String firstName,
            String lastName,
            String phone
    ) {}
}
