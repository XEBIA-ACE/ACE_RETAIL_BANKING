package com.banking.adapter.out.persistence.mapper;

import com.banking.adapter.out.persistence.entity.AccountJpaEntity;
import com.banking.domain.model.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toDomain(AccountJpaEntity entity) {
        return Account.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .customerId(entity.getCustomerId())
                .accountNumber(entity.getAccountNumber())
                .accountType(entity.getAccountType())
                .balance(entity.getBalance())
                .currency(entity.getCurrency())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public AccountJpaEntity toEntity(Account domain) {
        return AccountJpaEntity.builder()
                .id(domain.getId())
                .externalId(domain.getExternalId())
                .customerId(domain.getCustomerId())
                .accountNumber(domain.getAccountNumber())
                .accountType(domain.getAccountType())
                .balance(domain.getBalance())
                .currency(domain.getCurrency())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
