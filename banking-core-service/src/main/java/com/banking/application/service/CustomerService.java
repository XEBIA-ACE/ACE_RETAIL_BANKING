package com.banking.application.service;

import com.banking.application.port.in.CustomerUseCase;
import com.banking.application.port.out.CustomerRepository;
import com.banking.domain.exception.BusinessRuleException;
import com.banking.domain.exception.ResourceNotFoundException;
import com.banking.domain.model.Customer;
import com.banking.domain.model.CustomerStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService implements CustomerUseCase {

    private final CustomerRepository customerRepository;

    @Override
    public Customer onboardCustomer(OnboardCustomerCommand command) {
        if (customerRepository.existsByEmail(command.email())) {
            throw new BusinessRuleException("Customer with email already exists: " + command.email());
        }
        Customer customer = Customer.builder()
                .externalId(UUID.randomUUID().toString())
                .firstName(command.firstName())
                .lastName(command.lastName())
                .email(command.email())
                .phone(command.phone())
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Customer saved = customerRepository.save(customer);
        log.info("Customer onboarded: {}", saved.getExternalId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findCustomerById(String externalId) {
        return customerRepository.findByExternalId(externalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> listCustomers(int page, int size) {
        return customerRepository.findAll(page, size);
    }

    @Override
    public Customer updateCustomer(String externalId, UpdateCustomerCommand command) {
        Customer existing = customerRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", externalId));
        Customer updated = existing.toBuilder()
                .firstName(command.firstName())
                .lastName(command.lastName())
                .phone(command.phone())
                .updatedAt(LocalDateTime.now())
                .build();
        return customerRepository.save(updated);
    }

    @Override
    public void deactivateCustomer(String externalId) {
        Customer existing = customerRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", externalId));
        Customer deactivated = existing.toBuilder()
                .status(CustomerStatus.INACTIVE)
                .updatedAt(LocalDateTime.now())
                .build();
        customerRepository.save(deactivated);
        log.info("Customer deactivated: {}", externalId);
    }
}
