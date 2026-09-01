package com.banking.adapter.out.persistence.repository;

import com.banking.adapter.out.persistence.entity.CustomerJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, Long> {

    Optional<CustomerJpaEntity> findByExternalId(String externalId);

    Optional<CustomerJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<CustomerJpaEntity> findAll(Pageable pageable);
}
