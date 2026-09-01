package com.banking.application.service;

import com.banking.application.port.in.AccountUseCase;
import com.banking.application.port.out.AccountRepository;
import com.banking.application.port.out.CustomerRepository;
import com.banking.domain.exception.BusinessRuleException;
import com.banking.domain.exception.ResourceNotFoundException;
import com.banking.domain.model.Account;
import com.banking.domain.model.AccountStatus;
import com.banking.domain.model.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements AccountUseCase {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    @Override
    public Account openAccount(OpenAccountCommand command) {
        Customer customer = customerRepository.findByExternalId(command.customerExternalId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", command.customerExternalId()));

        String accountNumber = generateAccountNumber();
        Account account = Account.builder()
                .externalId(UUID.randomUUID().toString())
                .customerId(customer.getId())
                .accountNumber(accountNumber)
                .accountType(command.accountType())
                .balance(BigDecimal.ZERO)
                .currency(command.currency())
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Account saved = accountRepository.save(account);
        log.info("Account opened: {} for customer: {}", saved.getExternalId(), command.customerExternalId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findAccountById(String externalId) {
        return accountRepository.findByExternalId(externalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> listAccountsByCustomer(String customerExternalId) {
        Customer customer = customerRepository.findByExternalId(customerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerExternalId));
        return accountRepository.findByCustomerId(customer.getId());
    }

    @Override
    public Account closeAccount(String externalId) {
        Account existing = accountRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", externalId));
        if (existing.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessRuleException("Cannot close account with non-zero balance: " + externalId);
        }
        Account closed = existing.toBuilder()
                .status(AccountStatus.CLOSED)
                .updatedAt(LocalDateTime.now())
                .build();
        return accountRepository.save(closed);
    }

    private String generateAccountNumber() {
        return "ACC" + System.currentTimeMillis();
    }
}
