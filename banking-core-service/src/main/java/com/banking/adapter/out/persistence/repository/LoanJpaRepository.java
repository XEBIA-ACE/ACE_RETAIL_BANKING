package com.banking.adapter.out.persistence.repository;

import com.banking.adapter.out.persistence.entity.LoanJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanJpaRepository extends JpaRepository<LoanJpaEntity, Long> {

    Optional<LoanJpaEntity> findByExternalId(String externalId);

    List<LoanJpaEntity> findByCustomerId(Long customerId);
}
