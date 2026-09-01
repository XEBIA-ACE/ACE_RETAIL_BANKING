package com.banking.adapter.out.persistence;

import com.banking.adapter.out.persistence.mapper.LoanMapper;
import com.banking.adapter.out.persistence.repository.LoanJpaRepository;
import com.banking.application.port.out.LoanRepository;
import com.banking.domain.model.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LoanPersistenceAdapter implements LoanRepository {

    private final LoanJpaRepository jpaRepository;
    private final LoanMapper mapper;

    @Override
    public Loan save(Loan loan) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(loan)));
    }

    @Override
    public Optional<Loan> findByExternalId(String externalId) {
        return jpaRepository.findByExternalId(externalId).map(mapper::toDomain);
    }

    @Override
    public List<Loan> findByCustomerId(Long customerId) {
        return jpaRepository.findByCustomerId(customerId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
