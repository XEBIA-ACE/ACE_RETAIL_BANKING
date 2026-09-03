```java
package com.banking.application.port.in;

import com.banking.domain.model.Payee;

import java.util.List;

/**
 * Inbound port defining the Payee use-case contract.
 */
public interface PayeeUseCase {

    /**
     * Add a new payee for the given customer.
     *
     * @param customerExternalId the external ID of the owning customer
     * @param command            the add-payee command
     * @return the persisted Payee domain object
     */
    Payee addPayee(String customerExternalId, AddPayeeCommand command);

    /**
     * Update an existing payee.
     *
     * @param customerExternalId the external ID of the owning customer
     * @param payeeExternalId    the external ID of the payee to update
     * @param command            the update-payee command
     * @return the updated Payee domain object
     */
    Payee updatePayee(String customerExternalId, String payeeExternalId, UpdatePayeeCommand command);

    /**
     * Remove a payee.
     *
     * @param customerExternalId the external ID of the owning customer
     * @param payeeExternalId    the external ID of the payee to remove
     */
    void removePayee(String customerExternalId, String payeeExternalId);

    /**
     * List all payees belonging to the given customer.
     *
     * @param customerExternalId the external ID of the customer
     * @return list of Payee domain objects
     */
    List<Payee> listPayees(String customerExternalId);

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    record AddPayeeCommand(
            String payeeName,
            String accountNumber,
            String bankCode,
            String bankName,
            String nickname,
            String currency
    ) {}

    record UpdatePayeeCommand(
            String payeeName,
            String accountNumber,
            String bankCode,
            String bankName,
            String nickname,
            String currency
    ) {}
}
```