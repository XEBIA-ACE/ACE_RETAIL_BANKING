package com.banking.application.service;

import com.banking.adapter.in.web.dto.ProfileResponse;
import com.banking.application.port.in.ProfileUseCase;
import com.banking.application.port.out.CustomerRepository;
import com.banking.domain.exception.ResourceNotFoundException;
import com.banking.domain.model.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that implements {@link ProfileUseCase}.
 * Fetches the customer record from the repository and maps it to a {@link ProfileResponse}.
 * Internal database IDs are never exposed; only {@code externalId} is returned.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService implements ProfileUseCase {

    private final CustomerRepository customerRepository;

    /**
     * {@inheritDoc}
     *
     * <p>Logs at INFO on success (email masked). Logs at WARN when the customer is not found.
     */
    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String customerExternalId) {
        Customer customer = customerRepository.findByExternalId(customerExternalId)
                .orElseThrow(() -> {
                    log.warn("Profile not found for externalId={}", customerExternalId);
                    return new ResourceNotFoundException(
                            "Customer not found: " + customerExternalId);
                });

        log.info("Profile fetched for externalId={}, email=***", customerExternalId);

        return ProfileResponse.builder()
                .externalId(customer.getExternalId())
                .name(customer.getFirstName() + " " + customer.getLastName())
                .email(customer.getEmail())
                .registrationDate(customer.getCreatedAt().toString())
                .accountStatus(customer.getStatus().name())
                .build();
    }
}
