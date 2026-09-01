package com.banking.adapter.in.web;

import com.banking.adapter.in.web.dto.RecordTransactionRequest;
import com.banking.adapter.in.web.dto.TransactionResponse;
import com.banking.application.port.in.TransactionUseCase;
import com.banking.domain.model.Transaction;
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
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management")
public class TransactionController {

    private final TransactionUseCase transactionUseCase;

    @PostMapping
    @Operation(summary = "Record a new transaction")
    public ResponseEntity<TransactionResponse> recordTransaction(
            @Valid @RequestBody RecordTransactionRequest request) {
        Transaction transaction = transactionUseCase.recordTransaction(
                new TransactionUseCase.RecordTransactionCommand(
                        request.getAccountExternalId(),
                        request.getTransactionType(),
                        request.getAmount(),
                        request.getCurrency(),
                        request.getDescription(),
                        request.getReference()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(transaction));
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Get transaction by external ID")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String externalId) {
        return transactionUseCase.findTransactionById(externalId)
                .map(t -> ResponseEntity.ok(toResponse(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List transactions by account (paginated)")
    public ResponseEntity<List<TransactionResponse>> listTransactions(
            @RequestParam String accountExternalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<TransactionResponse> transactions =
                transactionUseCase.listTransactionsByAccount(accountExternalId, page, size)
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .externalId(transaction.getExternalId())
                .transactionType(transaction.getTransactionType().name())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .reference(transaction.getReference())
                .status(transaction.getStatus().name())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
