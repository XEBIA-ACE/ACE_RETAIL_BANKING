package com.banking.adapter.in.web.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class AccountResponse {
    String externalId;
    String accountNumber;
    String accountType;
    BigDecimal balance;
    String currency;
    String status;
    LocalDateTime createdAt;
}
