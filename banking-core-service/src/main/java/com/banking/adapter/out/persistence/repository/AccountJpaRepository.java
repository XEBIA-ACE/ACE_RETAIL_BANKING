package com.banking.adapter.out.persistence.repository;

import com.banking.adapter.out.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, Long> {

    Optional<AccountJpaEntity> findByExternalId(String externalId);

    Optional<AccountJpaEntity> findByAccountNumber(String accountNumber);

    List<AccountJpaEntity> findByCustomerId(Long customerId);

    boolean existsByAccountNumber(String accountNumber);
}
