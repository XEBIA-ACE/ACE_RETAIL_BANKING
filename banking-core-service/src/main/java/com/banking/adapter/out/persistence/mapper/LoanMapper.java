package com.banking.adapter.out.persistence.mapper;

import com.banking.adapter.out.persistence.entity.LoanJpaEntity;
import com.banking.domain.model.Loan;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

    public Loan toDomain(LoanJpaEntity entity) {
        return Loan.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .customerId(entity.getCustomerId())
                .principal(entity.getPrincipal())
                .interestRate(entity.getInterestRate())
                .termMonths(entity.getTermMonths())
                .monthlyPayment(entity.getMonthlyPayment())
                .outstanding(entity.getOutstanding())
                .currency(entity.getCurrency())
                .status(entity.getStatus())
                .disbursedAt(entity.getDisbursedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public LoanJpaEntity toEntity(Loan domain) {
        return LoanJpaEntity.builder()
                .id(domain.getId())
                .externalId(domain.getExternalId())
                .customerId(domain.getCustomerId())
                .principal(domain.getPrincipal())
                .interestRate(domain.getInterestRate())
                .termMonths(domain.getTermMonths())
                .monthlyPayment(domain.getMonthlyPayment())
                .outstanding(domain.getOutstanding())
                .currency(domain.getCurrency())
                .status(domain.getStatus())
                .disbursedAt(domain.getDisbursedAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
