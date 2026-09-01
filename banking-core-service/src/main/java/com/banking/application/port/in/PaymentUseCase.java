package com.banking.application.port.in;

import com.banking.domain.model.Payment;
import com.banking.domain.model.PaymentType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Inbound port — use cases for payment handling.
 */
public interface PaymentUseCase {

    Payment initiatePayment(InitiatePaymentCommand command);

    Payment processPayment(String externalId);

    Optional<Payment> findPaymentById(String externalId);

    List<Payment> listPaymentsByAccount(String accountExternalId, int page, int size);

    record InitiatePaymentCommand(
            String sourceAccountExternalId,
            String targetAccountExternalId,
            BigDecimal amount,
            String currency,
            PaymentType paymentType,
            String description
    ) {}
}
