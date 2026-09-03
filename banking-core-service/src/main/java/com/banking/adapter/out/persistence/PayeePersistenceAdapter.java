package com.banking.adapter.out.persistence;

import com.banking.application.port.out.PayeeRepository;
import com.banking.domain.model.Payee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Persistence adapter implementing the {@link PayeeRepository} outbound port.
 * Delegates to {@link PayeeJpaRepository} and maps between {@link PayeeJpaEntity}
 * and the {@link Payee} domain model.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayeePersistenceAdapter implements PayeeRepository {

    private final PayeeJpaRepository payeeJpaRepository;

    /**
     * Persists a {@link Payee} domain object and returns the saved state.
     *
     * @param payee the domain payee to save (create or update)
     * @return the saved {@link Payee} with any generated fields populated
     */
    @Override
    @Transactional
    public Payee save(Payee payee) {
        log.info("Persisting payee with externalId={}", payee.getExternalId());
        PayeeJpaEntity entity = toEntity(payee);
        PayeeJpaEntity saved = payeeJpaRepository.save(entity);
        return toDomain(saved);
    }

    /**
     * Finds a payee by its public external identifier.
     *
     * @param externalId the UUID string assigned to the payee
     * @return an {@link Optional} containing the domain payee if found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Payee> findByExternalId(String externalId) {
        log.info("Looking up payee by externalId={}", externalId);
        return payeeJpaRepository.findByExternalId(externalId)
                .map(this::toDomain);
    }

    /**
     * Returns all payees belonging to the given internal customer identifier.
     *
     * @param customerId the internal (database) customer ID
     * @return list of domain payees; empty list if none found
     */
    @Override
    @Transactional(readOnly = true)
    public List<Payee> findByCustomerId(Long customerId) {
        log.info("Fetching payees for customerId={}", customerId);
        return payeeJpaRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Deletes the payee identified by the given external identifier.
     * No-op if no payee with that identifier exists.
     *
     * @param externalId the UUID string of the payee to delete
     */
    @Override
    @Transactional
    public void deleteByExternalId(String externalId) {
        log.info("Deleting payee with externalId={}", externalId);
        payeeJpaRepository.deleteByExternalId(externalId);
    }

    /**
     * Checks whether a payee already exists for the given customer and account number.
     * Used to enforce the duplicate-prevention business rule.
     *
     * @param customerId    the internal customer ID
     * @param accountNumber the destination account number
     * @return {@code true} if a payee with the same customer/account combination exists
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByCustomerIdAndAccountNumber(Long customerId, String accountNumber) {
        log.info("Checking duplicate payee for customerId={}, accountNumber={}", customerId, accountNumber);
        return payeeJpaRepository.existsByCustomerIdAndAccountNumber(customerId, accountNumber);
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    /**
     * Maps a {@link Payee} domain object to a {@link PayeeJpaEntity}.
     * The {@code id} field is carried over so that Spring Data JPA can distinguish
     * between insert (id == null) and update (id != null).
     */
    private PayeeJpaEntity toEntity(Payee payee) {
        return PayeeJpaEntity.builder()
                .id(payee.getId())
                .externalId(payee.getExternalId())
                .customerId(payee.getCustomerId())
                .payeeName(payee.getPayeeName())
                .accountNumber(payee.getAccountNumber())
                .bankCode(payee.getBankCode())
                .bankName(payee.getBankName())
                .nickname(payee.getNickname())
                .currency(payee.getCurrency())
                .createdAt(payee.getCreatedAt())
                .updatedAt(payee.getUpdatedAt())
                .build();
    }

    /**
     * Maps a {@link PayeeJpaEntity} to a {@link Payee} domain object.
     */
    private Payee toDomain(PayeeJpaEntity entity) {
        return Payee.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .customerId(entity.getCustomerId())
                .payeeName(entity.getPayeeName())
                .accountNumber(entity.getAccountNumber())
                .bankCode(entity.getBankCode())
                .bankName(entity.getBankName())
                .nickname(entity.getNickname())
                .currency(entity.getCurrency())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}