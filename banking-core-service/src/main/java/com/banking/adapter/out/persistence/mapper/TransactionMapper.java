package com.banking.adapter.out.persistence.mapper;

import com.banking.adapter.out.persistence.entity.TransactionJpaEntity;
import com.banking.domain.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toDomain(TransactionJpaEntity entity) {
        return Transaction.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .accountId(entity.getAccountId())
                .transactionType(entity.getTransactionType())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .description(entity.getDescription())
                .reference(entity.getReference())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TransactionJpaEntity toEntity(Transaction domain) {
        return TransactionJpaEntity.builder()
                .id(domain.getId())
                .externalId(domain.getExternalId())
                .accountId(domain.getAccountId())
                .transactionType(domain.getTransactionType())
                .amount(domain.getAmount())
                .currency(domain.getCurrency())
                .description(domain.getDescription())
                .reference(domain.getReference())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
