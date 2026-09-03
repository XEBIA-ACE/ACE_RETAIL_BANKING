package com.banking.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link PayeeJpaEntity}.
 * Method names follow Spring Data naming conventions so no custom JPQL is needed.
 */
public interface PayeeJpaRepository extends JpaRepository<PayeeJpaEntity, Long> {

    Optional<PayeeJpaEntity> findByExternalId(String externalId);

    List<PayeeJpaEntity> findAllByCustomerId(Long customerId);

    void deleteByExternalId(String externalId);

    boolean existsByCustomerIdAndAccountNumber(Long customerId, String accountNumber);
}
