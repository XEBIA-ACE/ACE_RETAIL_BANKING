package com.bank.core.account.controller;

import com.bank.core.account.dto.BalanceResponse;
import com.bank.core.account.service.BalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Balances", description = "Real-time account balance access")
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/{accountId}/balance")
    @PreAuthorize("@accountOwnershipChecker.check(#accountId, authentication)")
    @Operation(summary = "Get the current account balance")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance returned"),
            @ApiResponse(responseCode = "403", description = "Account is not owned by the caller"),
            @ApiResponse(responseCode = "404", description = "Account does not exist")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String accountId) {
        return ResponseEntity.ok(balanceService.getBalance(accountId));
    }
}
