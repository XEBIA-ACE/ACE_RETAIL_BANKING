package com.banking.application.port.out;

import com.banking.domain.model.Payment;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port — persistence operations for Payment.
 */
public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByExternalId(String externalId);

    List<Payment> findBySourceAccount(Long accountId, int page, int size);
}
