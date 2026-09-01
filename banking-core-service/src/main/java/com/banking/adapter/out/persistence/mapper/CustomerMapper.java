package com.banking.adapter.out.persistence.mapper;

import com.banking.adapter.out.persistence.entity.CustomerJpaEntity;
import com.banking.domain.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toDomain(CustomerJpaEntity entity) {
        return Customer.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CustomerJpaEntity toEntity(Customer domain) {
        return CustomerJpaEntity.builder()
                .id(domain.getId())
                .externalId(domain.getExternalId())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .email(domain.getEmail())
                .phone(domain.getPhone())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
