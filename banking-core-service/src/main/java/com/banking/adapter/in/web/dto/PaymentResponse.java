package com.banking.adapter.in.web.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class PaymentResponse {
    String externalId;
    Long sourceAccount;
    Long targetAccount;
    BigDecimal amount;
    String currency;
    String paymentType;
    String description;
    String status;
    LocalDateTime createdAt;
}
