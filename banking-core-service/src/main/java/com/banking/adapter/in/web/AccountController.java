package com.banking.adapter.in.web;

import com.banking.adapter.in.web.dto.AccountBalanceSummaryResponse;
import com.banking.application.service.AccountService;
import com.banking.domain.model.Account;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST adapter for account-related operations.
 */
@RestController
@RequestMapping("/api/v1/customers/{customerExternalId}/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management")
public class AccountController {

    private final AccountService accountService;

    /**
     * Returns the current balance for every account belonging to the given customer.
     *
     * @param customerExternalId public customer identifier
     * @return list of {@link AccountBalanceSummaryResponse} — HTTP 200, or 404 if customer unknown
     */
    @GetMapping("/balances")
    @Operation(summary = "Get account balances for a customer")
    public ResponseEntity<List<AccountBalanceSummaryResponse>> getAccountBalances(
            @PathVariable String customerExternalId) {

        List<AccountBalanceSummaryResponse> balances = accountService
                .getAccountBalances(customerExternalId)
                .stream()
                .map(this::toBalanceSummary)
                .collect(Collectors.toList());

        return ResponseEntity.ok(balances);
    }

    // -----------------------------------------------------------------------
    // Mapping helpers
    // -----------------------------------------------------------------------

    private AccountBalanceSummaryResponse toBalanceSummary(Account account) {
        return AccountBalanceSummaryResponse.builder()
                .accountExternalId(account.getExternalId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .build();
    }
}
