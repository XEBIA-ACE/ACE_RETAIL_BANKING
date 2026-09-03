package com.bank.core.dashboard.controller;

import com.bank.core.account.dto.DashboardResponseDto;
import com.bank.core.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Consolidated account dashboard endpoints")
public class DashboardController {

    private final AccountService accountService;

    @GetMapping("/accounts")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get consolidated account dashboard",
            description = "Returns all accounts belonging to the authenticated customer, grouped by account type.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<DashboardResponseDto> getAccounts(Authentication authentication) {
        String customerExternalId = authentication.getName();
        log.info("Dashboard accounts request received for customer: {}", customerExternalId);

        DashboardResponseDto response = accountService.getAccountsByCustomer(customerExternalId);

        log.info("Dashboard accounts response returned for customer: {}", customerExternalId);
        return ResponseEntity.ok(response);
    }
}