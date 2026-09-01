package com.banking.adapter.out.persistence;

import com.banking.adapter.out.persistence.mapper.PaymentMapper;
import com.banking.adapter.out.persistence.repository.PaymentJpaRepository;
import com.banking.application.port.out.PaymentRepository;
import com.banking.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentMapper mapper;

    @Override
    public Payment save(Payment payment) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(payment)));
    }

    @Override
    public Optional<Payment> findByExternalId(String externalId) {
        return jpaRepository.findByExternalId(externalId).map(mapper::toDomain);
    }

    @Override
    public List<Payment> findBySourceAccount(Long accountId, int page, int size) {
        return jpaRepository.findBySourceAccount(accountId, PageRequest.of(page, size))
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
