```java
package com.banking.application.service;

import com.banking.application.port.in.PayeeUseCase;
import com.banking.application.port.out.CustomerRepository;
import com.banking.application.port.out.PayeeRepository;
import com.banking.domain.exception.BusinessRuleException;
import com.banking.domain.exception.ResourceNotFoundException;
import com.banking.domain.model.Customer;
import com.banking.domain.model.CustomerStatus;
import com.banking.domain.model.Payee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service implementing PayeeUseCase.
 * Handles add, update, remove, and list operations for customer payees.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayeeService implements PayeeUseCase {

    private final PayeeRepository payeeRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public Payee addPayee(AddPayeeCommand command) {
        log.info("Adding payee for customer externalId={}, accountNumber={}",
                command.customerExternalId(), command.accountNumber());

        Customer customer = resolveActiveCustomer(command.customerExternalId());

        if (payeeRepository.existsByCustomerIdAndAccountNumber(customer.getId(), command.accountNumber())) {
            log.info("Duplicate payee detected for customerId={}, accountNumber={}",
                    customer.getId(), command.accountNumber());
            throw new BusinessRuleException("Payee with this account number already exists");
        }

        LocalDateTime now = LocalDateTime.now();
        Payee payee = Payee.builder()
                .externalId(UUID.randomUUID().toString())
                .customerId(customer.getId())
                .payeeName(command.payeeName())
                .accountNumber(command.accountNumber())
                .bankCode(command.bankCode())
                .bankName(command.bankName())
                .nickname(command.nickname())
                .currency(command.currency())
                .createdAt(now)
                .updatedAt(now)
                .build();

        Payee saved = payeeRepository.save(payee);
        log.info("Payee created with externalId={} for customerId={}", saved.getExternalId(), customer.getId());
        return saved;
    }

    @Override
    @Transactional
    public Payee updatePayee(String customerExternalId, String payeeExternalId, UpdatePayeeCommand command) {
        log.info("Updating payee externalId={} for customer externalId={}", payeeExternalId, customerExternalId);

        Customer customer = resolveActiveCustomer(customerExternalId);
        Payee payee = resolvePayeeWithOwnershipCheck(payeeExternalId, customer);

        Payee updated = payee.toBuilder()
                .payeeName(command.payeeName() != null ? command.payeeName() : payee.getPayeeName())
                .accountNumber(command.accountNumber() != null ? command.accountNumber() : payee.getAccountNumber())
                .bankCode(command.bankCode() != null ? command.bankCode() : payee.getBankCode())
                .bankName(command.bankName() != null ? command.bankName() : payee.getBankName())
                .nickname(command.nickname() != null ? command.nickname() : payee.getNickname())
                .currency(command.currency() != null ? command.currency() : payee.getCurrency())
                .updatedAt(LocalDateTime.now())
                .build();

        Payee saved = payeeRepository.save(updated);
        log.info("Payee updated with externalId={} for customerId={}", saved.getExternalId(), customer.getId());
        return saved;
    }

    @Override
    @Transactional
    public void removePayee(String customerExternalId, String payeeExternalId) {
        log.info("Removing payee externalId={} for customer externalId={}", payeeExternalId, customerExternalId);

        Customer customer = resolveActiveCustomer(customerExternalId);
        resolvePayeeWithOwnershipCheck(payeeExternalId, customer);

        payeeRepository.deleteByExternalId(payeeExternalId);
        log.info("Payee deleted with externalId={} for customerId={}", payeeExternalId, customer.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payee> listPayees(String customerExternalId) {
        log.info("Listing payees for customer externalId={}", customerExternalId);

        Customer customer = resolveActiveCustomer(customerExternalId);
        List<Payee> payees = payeeRepository.findByCustomerId(customer.getId());

        log.info("Found {} payees for customerId={}", payees.size(), customer.getId());
        return payees;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Customer resolveActiveCustomer(String customerExternalId) {
        Customer customer = customerRepository.findByExternalId(customerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with externalId: " + customerExternalId));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new ResourceNotFoundException(
                    "Customer is not active with externalId: " + customerExternalId);
        }

        return customer;
    }

    private Payee resolvePayeeWithOwnershipCheck(String payeeExternalId, Customer customer) {
        Payee payee = payeeRepository.findByExternalId(payeeExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payee not found with externalId: " + payeeExternalId));

        if (!payee.getCustomerId().equals(customer.getId())) {
            throw new ResourceNotFoundException(
                    "Payee not found with externalId: " + payeeExternalId);
        }

        return payee;
    }
}
```