package com.banking.adapter.out.persistence.repository;

import com.banking.adapter.out.persistence.entity.PaymentJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, Long> {

    Optional<PaymentJpaEntity> findByExternalId(String externalId);

    List<PaymentJpaEntity> findBySourceAccount(Long sourceAccount, Pageable pageable);
}
