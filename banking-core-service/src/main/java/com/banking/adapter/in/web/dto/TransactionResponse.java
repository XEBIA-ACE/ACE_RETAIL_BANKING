package com.banking.adapter.in.web.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class TransactionResponse {
    String externalId;
    String transactionType;
    BigDecimal amount;
    String currency;
    String description;
    String reference;
    String status;
    LocalDateTime createdAt;
}
