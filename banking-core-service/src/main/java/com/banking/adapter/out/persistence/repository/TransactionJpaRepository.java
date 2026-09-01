package com.banking.adapter.out.persistence.repository;

import com.banking.adapter.out.persistence.entity.TransactionJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {

    Optional<TransactionJpaEntity> findByExternalId(String externalId);

    List<TransactionJpaEntity> findByAccountId(Long accountId, Pageable pageable);
}
