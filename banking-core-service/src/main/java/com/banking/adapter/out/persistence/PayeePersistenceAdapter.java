package com.banking.adapter.out.persistence;

import com.banking.application.port.out.PayeeRepository;
import com.banking.domain.model.Payee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence adapter implementing the {@link PayeeRepository} outbound port.
 * Delegates to {@link PayeeJpaRepository} (Spring Data JPA) and maps between
 * {@link PayeeJpaEntity} and the {@link Payee} domain model.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PayeePersistenceAdapter implements PayeeRepository {

    private final PayeeJpaRepository payeeJpaRepository;

    /**
     * Persist a new payee or update an existing one.
     * <ul>
     *   <li>If {@code externalId} is null a new UUID is generated.</li>
     *   <li>{@code createdAt} is set only when the entity is new (no DB id yet).</li>
     *   <li>{@code updatedAt} is always refreshed to the current timestamp.</li>
     * </ul>
     */
    @Override
    @Transactional
    public Payee save(Payee payee) {
        LocalDateTime now = LocalDateTime.now();

        // Determine whether this is a create or an update
        boolean isNew = (payee.getId() == null);

        PayeeJpaEntity entity;

        if (!isNew) {
            // Load the existing row so JPA can issue an UPDATE
            entity = payeeJpaRepository.findById(payee.getId())
                    .orElseGet(PayeeJpaEntity::new);
        } else {
            entity = new PayeeJpaEntity();
        }

        // Generate externalId for brand-new payees
        String externalId = (payee.getExternalId() != null && !payee.getExternalId().isBlank())
                ? payee.getExternalId()
                : UUID.randomUUID().toString();

        entity.setExternalId(externalId);
        entity.setCustomerId(payee.getCustomerId());
        entity.setPayeeName(payee.getPayeeName());
        entity.setAccountNumber(payee.getAccountNumber());
        entity.setBankCode(payee.getBankCode());
        entity.setBankName(payee.getBankName());
        entity.setNickname(payee.getNickname());
        entity.setCurrency(payee.getCurrency());

        // createdAt is set once on creation; updatedAt is always refreshed
        entity.setCreatedAt(isNew ? now : (payee.getCreatedAt() != null ? payee.getCreatedAt() : now));
        entity.setUpdatedAt(now);

        PayeeJpaEntity saved = payeeJpaRepository.save(entity);
        log.info("Payee saved: externalId={}, customerId={}", saved.getExternalId(), saved.getCustomerId());
        return toDomain(saved);
    }

    /**
     * Find a payee by its public external identifier.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Payee> findByExternalId(String externalId) {
        return payeeJpaRepository.findByExternalId(externalId)
                .map(this::toDomain);
    }

    /**
     * Return all payees belonging to the given internal customer ID.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Payee> findAllByCustomerId(Long customerId) {
        return payeeJpaRepository.findAllByCustomerId(customerId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Delete a payee by its public external identifier.
     */
    @Override
    @Transactional
    public void deleteByExternalId(String externalId) {
        log.info("Deleting payee with externalId={}", externalId);
        payeeJpaRepository.deleteByExternalId(externalId);
    }

    /**
     * Check whether a payee with the given account number already exists
     * for the specified customer (duplicate prevention).
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByCustomerIdAndAccountNumber(Long customerId, String accountNumber) {
        return payeeJpaRepository.existsByCustomerIdAndAccountNumber(customerId, accountNumber);
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    /**
     * Map a {@link PayeeJpaEntity} to the {@link Payee} domain model.
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
