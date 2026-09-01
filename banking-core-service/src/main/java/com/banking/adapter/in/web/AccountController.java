package com.banking.adapter.in.web;

import com.banking.adapter.in.web.dto.AccountResponse;
import com.banking.adapter.in.web.dto.OpenAccountRequest;
import com.banking.application.port.in.AccountUseCase;
import com.banking.domain.model.Account;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management")
public class AccountController {

    private final AccountUseCase accountUseCase;

    @PostMapping
    @Operation(summary = "Open a new account")
    public ResponseEntity<AccountResponse> openAccount(
            @Valid @RequestBody OpenAccountRequest request) {
        Account account = accountUseCase.openAccount(
                new AccountUseCase.OpenAccountCommand(
                        request.getCustomerExternalId(),
                        request.getAccountType(),
                        request.getCurrency()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(account));
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Get account by external ID")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String externalId) {
        return accountUseCase.findAccountById(externalId)
                .map(a -> ResponseEntity.ok(toResponse(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List accounts by customer")
    public ResponseEntity<List<AccountResponse>> listAccountsByCustomer(
            @RequestParam String customerExternalId) {
        List<AccountResponse> accounts = accountUseCase.listAccountsByCustomer(customerExternalId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    @DeleteMapping("/{externalId}")
    @Operation(summary = "Close an account")
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable String externalId) {
        return ResponseEntity.ok(toResponse(accountUseCase.closeAccount(externalId)));
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .externalId(account.getExternalId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType().name())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus().name())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
