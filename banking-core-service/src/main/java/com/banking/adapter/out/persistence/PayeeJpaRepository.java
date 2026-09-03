package com.banking.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link PayeeJpaEntity}.
 */
@Repository
public interface PayeeJpaRepository extends JpaRepository<PayeeJpaEntity, Long> {

    Optional<PayeeJpaEntity> findByExternalId(String externalId);

    List<PayeeJpaEntity> findByCustomerId(Long customerId);

    void deleteByExternalId(String externalId);

    boolean existsByCustomerIdAndAccountNumber(Long customerId, String accountNumber);
}