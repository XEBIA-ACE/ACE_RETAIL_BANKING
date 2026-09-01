package com.banking.adapter.in.web;

import com.banking.adapter.in.web.dto.ApplyForLoanRequest;
import com.banking.adapter.in.web.dto.LoanResponse;
import com.banking.application.port.in.LoanUseCase;
import com.banking.domain.model.Loan;
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
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Loan processing")
public class LoanController {

    private final LoanUseCase loanUseCase;

    @PostMapping
    @Operation(summary = "Apply for a loan")
    public ResponseEntity<LoanResponse> applyForLoan(
            @Valid @RequestBody ApplyForLoanRequest request) {
        Loan loan = loanUseCase.applyForLoan(
                new LoanUseCase.ApplyForLoanCommand(
                        request.getCustomerExternalId(),
                        request.getPrincipal(),
                        request.getInterestRate(),
                        request.getTermMonths(),
                        request.getCurrency()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(loan));
    }

    @PostMapping("/{externalId}/approve")
    @Operation(summary = "Approve a loan")
    public ResponseEntity<LoanResponse> approveLoan(@PathVariable String externalId) {
        return ResponseEntity.ok(toResponse(loanUseCase.approveLoan(externalId)));
    }

    @PostMapping("/{externalId}/disburse")
    @Operation(summary = "Disburse a loan")
    public ResponseEntity<LoanResponse> disburseLoan(@PathVariable String externalId) {
        return ResponseEntity.ok(toResponse(loanUseCase.disburseLoan(externalId)));
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Get loan by external ID")
    public ResponseEntity<LoanResponse> getLoan(@PathVariable String externalId) {
        return loanUseCase.findLoanById(externalId)
                .map(l -> ResponseEntity.ok(toResponse(l)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List loans by customer")
    public ResponseEntity<List<LoanResponse>> listLoansByCustomer(
            @RequestParam String customerExternalId) {
        List<LoanResponse> loans = loanUseCase.listLoansByCustomer(customerExternalId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loans);
    }

    private LoanResponse toResponse(Loan loan) {
        return LoanResponse.builder()
                .externalId(loan.getExternalId())
                .principal(loan.getPrincipal())
                .interestRate(loan.getInterestRate())
                .termMonths(loan.getTermMonths())
                .monthlyPayment(loan.getMonthlyPayment())
                .outstanding(loan.getOutstanding())
                .currency(loan.getCurrency())
                .status(loan.getStatus().name())
                .createdAt(loan.getCreatedAt())
                .build();
    }
}
