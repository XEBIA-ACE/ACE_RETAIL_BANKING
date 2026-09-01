package com.banking.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment domain entity.
 */
@Value
@Builder(toBuilder = true)
public class Payment {

    Long id;
    String externalId;
    Long sourceAccount;
    Long targetAccount;
    BigDecimal amount;
    String currency;
    PaymentType paymentType;
    String description;
    PaymentStatus status;
    LocalDateTime processedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
