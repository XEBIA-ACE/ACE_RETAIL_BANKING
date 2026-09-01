package com.banking.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction domain entity.
 */
@Value
@Builder(toBuilder = true)
public class Transaction {

    Long id;
    String externalId;
    Long accountId;
    TransactionType transactionType;
    BigDecimal amount;
    String currency;
    String description;
    String reference;
    TransactionStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
