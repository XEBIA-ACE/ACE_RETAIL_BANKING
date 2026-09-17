package com.bank.core.account.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BalanceResponse(
        String accountId,
        BigDecimal balance,
        String currency,
        Instant balanceTimestamp) {
}
