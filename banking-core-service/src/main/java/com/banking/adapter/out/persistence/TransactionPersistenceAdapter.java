package com.banking.adapter.out.persistence;

import com.banking.adapter.out.persistence.mapper.TransactionMapper;
import com.banking.adapter.out.persistence.repository.TransactionJpaRepository;
import com.banking.application.port.out.TransactionRepository;
import com.banking.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionMapper mapper;

    @Override
    public Transaction save(Transaction transaction) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(transaction)));
    }

    @Override
    public Optional<Transaction> findByExternalId(String externalId) {
        return jpaRepository.findByExternalId(externalId).map(mapper::toDomain);
    }

    @Override
    public List<Transaction> findByAccountId(Long accountId, int page, int size) {
        return jpaRepository.findByAccountId(accountId, PageRequest.of(page, size))
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
