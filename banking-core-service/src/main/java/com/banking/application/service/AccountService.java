package com.banking.application.service;

import com.banking.application.port.in.AccountUseCase;
import com.banking.application.port.out.AccountRepository;
import com.banking.application.port.out.CustomerRepository;
import com.banking.domain.exception.ResourceNotFoundException;
import com.banking.domain.model.Account;
import com.banking.domain.model.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service for account-related use cases.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService implements AccountUseCase {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    /**
     * Returns all accounts (with live balances) for the given customer.
     *
     * @param customerExternalId public customer identifier
     * @return list of {@link Account} domain objects
     * @throws ResourceNotFoundException if the customer does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountBalances(String customerExternalId) {
        Customer customer = customerRepository.findByExternalId(customerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + customerExternalId));

        List<Account> accounts = accountRepository.findByCustomerId(customer.getId());
        log.info("Retrieved {} account balance(s) for customer '{}'",
                accounts.size(), customerExternalId);
        return accounts;
    }

    /**
     * Lists all accounts belonging to a customer (alias for {@link #getAccountBalances}).
     *
     * @param customerExternalId public customer identifier
     * @return list of {@link Account} domain objects
     */
    @Override
    @Transactional(readOnly = true)
    public List<Account> listAccountsByCustomer(String customerExternalId) {
        return getAccountBalances(customerExternalId);
    }
}
