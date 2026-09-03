package com.bank.core.account.controller;

import com.bank.core.account.dto.DashboardResponse;
import com.bank.core.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Accounts", description = "Account management and dashboard endpoints")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/dashboard")
    @Operation(
            summary = "Get consolidated account dashboard",
            description = "Returns all linked accounts for the authenticated customer in a single consolidated view.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated request"),
            @ApiResponse(responseCode = "403", description = "Authenticated but not authorised"),
            @ApiResponse(responseCode = "500", description = "Unexpected server-side failure")
    })
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal UserDetails principal) {

        log.info("GET /api/v1/accounts/dashboard invoked for authenticated user");

        String customerId = principal.getUsername();
        DashboardResponse response = accountService.getDashboard(customerId);

        return ResponseEntity.ok(response);
    }
}