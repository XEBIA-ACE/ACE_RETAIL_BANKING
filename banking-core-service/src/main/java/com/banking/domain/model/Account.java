package com.banking.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account domain entity.
 */
@Value
@Builder(toBuilder = true)
public class Account {

    Long id;
    String externalId;
    Long customerId;
    String accountNumber;
    AccountType accountType;
    BigDecimal balance;
    String currency;
    AccountStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
