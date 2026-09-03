package com.banking.adapter.in.web.dto;

import com.banking.domain.model.AccountStatus;
import com.banking.domain.model.AccountType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Response DTO for the account balance summary endpoint.
 * Intentionally omits the internal database {@code id} field (FR-4).
 */
@Value
@Builder
public class AccountBalanceSummaryResponse {

    /** External (public) account identifier. */
    String accountExternalId;

    /** Human-readable account number. */
    String accountNumber;

    /** Type of account (CHECKING, SAVINGS, etc.). */
    AccountType accountType;

    /** Current balance — raw BigDecimal; formatting is a presentation concern. */
    BigDecimal balance;

    /** ISO 4217 currency code (e.g. "USD", "EUR"). */
    String currency;

    /** Current lifecycle status of the account. */
    AccountStatus status;
}
