package com.banking.adapter.out.persistence.mapper;

import com.banking.adapter.out.persistence.entity.PaymentJpaEntity;
import com.banking.domain.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toDomain(PaymentJpaEntity entity) {
        return Payment.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .sourceAccount(entity.getSourceAccount())
                .targetAccount(entity.getTargetAccount())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .paymentType(entity.getPaymentType())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .processedAt(entity.getProcessedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PaymentJpaEntity toEntity(Payment domain) {
        return PaymentJpaEntity.builder()
                .id(domain.getId())
                .externalId(domain.getExternalId())
                .sourceAccount(domain.getSourceAccount())
                .targetAccount(domain.getTargetAccount())
                .amount(domain.getAmount())
                .currency(domain.getCurrency())
                .paymentType(domain.getPaymentType())
                .description(domain.getDescription())
                .status(domain.getStatus())
                .processedAt(domain.getProcessedAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
