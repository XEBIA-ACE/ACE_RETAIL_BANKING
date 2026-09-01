package com.banking.adapter.in.web.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class LoanResponse {
    String externalId;
    BigDecimal principal;
    BigDecimal interestRate;
    Integer termMonths;
    BigDecimal monthlyPayment;
    BigDecimal outstanding;
    String currency;
    String status;
    LocalDateTime createdAt;
}
