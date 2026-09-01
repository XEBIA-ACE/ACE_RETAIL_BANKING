package com.banking.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Loan domain entity.
 */
@Value
@Builder(toBuilder = true)
public class Loan {

    Long id;
    String externalId;
    Long customerId;
    BigDecimal principal;
    BigDecimal interestRate;
    Integer termMonths;
    BigDecimal monthlyPayment;
    BigDecimal outstanding;
    String currency;
    LoanStatus status;
    LocalDateTime disbursedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
