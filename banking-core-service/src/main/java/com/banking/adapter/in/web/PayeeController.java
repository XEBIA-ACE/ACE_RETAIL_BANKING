```java
package com.banking.adapter.in.web;

import com.banking.application.port.in.PayeeUseCase;
import com.banking.domain.model.Payee;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customers/{customerExternalId}/payees")
@RequiredArgsConstructor
@Tag(name = "Payees", description = "Payee management")
public class PayeeController {

    private final PayeeUseCase payeeUseCase;

    // -------------------------------------------------------------------------
    // POST /api/v1/customers/{customerExternalId}/payees
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(summary = "Add a new payee")
    public ResponseEntity<PayeeResponse> addPayee(
            @PathVariable String customerExternalId,
            @Valid @RequestBody AddPayeeRequest request) {

        Payee payee = payeeUseCase.addPayee(
                customerExternalId,
                new PayeeUseCase.AddPayeeCommand(
                        request.getPayeeName(),
                        request.getAccountNumber(),
                        request.getBankCode(),
                        request.getBankName(),
                        request.getNickname(),
                        request.getCurrency()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(payee));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/customers/{customerExternalId}/payees
    // -------------------------------------------------------------------------

    @GetMapping
    @Operation(summary = "List all payees for a customer")
    public ResponseEntity<List<PayeeResponse>> listPayees(
            @PathVariable String customerExternalId) {

        List<PayeeResponse> responses = payeeUseCase.listPayees(customerExternalId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/customers/{customerExternalId}/payees/{payeeExternalId}
    // -------------------------------------------------------------------------

    @PutMapping("/{payeeExternalId}")
    @Operation(summary = "Update an existing payee")
    public ResponseEntity<PayeeResponse> updatePayee(
            @PathVariable String customerExternalId,
            @PathVariable String payeeExternalId,
            @Valid @RequestBody UpdatePayeeRequest request) {

        Payee payee = payeeUseCase.updatePayee(
                customerExternalId,
                payeeExternalId,
                new PayeeUseCase.UpdatePayeeCommand(
                        request.getPayeeName(),
                        request.getAccountNumber(),
                        request.getBankCode(),
                        request.getBankName(),
                        request.getNickname(),
                        request.getCurrency()
                )
        );
        return ResponseEntity.ok(toResponse(payee));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/customers/{customerExternalId}/payees/{payeeExternalId}
    // -------------------------------------------------------------------------

    @DeleteMapping("/{payeeExternalId}")
    @Operation(summary = "Remove a payee")
    public ResponseEntity<Void> removePayee(
            @PathVariable String customerExternalId,
            @PathVariable String payeeExternalId) {

        payeeUseCase.removePayee(customerExternalId, payeeExternalId);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Mapping helper
    // -------------------------------------------------------------------------

    private PayeeResponse toResponse(Payee payee) {
        return PayeeResponse.builder()
                .externalId(payee.getExternalId())
                .payeeName(payee.getPayeeName())
                .accountNumber(payee.getAccountNumber())
                .bankCode(payee.getBankCode())
                .bankName(payee.getBankName())
                .nickname(payee.getNickname())
                .currency(payee.getCurrency())
                .createdAt(payee.getCreatedAt())
                .updatedAt(payee.getUpdatedAt())
                .build();
    }

    // =========================================================================
    // DTOs
    // =========================================================================

    /**
     * Request body for adding a new payee.
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AddPayeeRequest {

        @NotBlank(message = "payeeName is required")
        @Size(max = 255)
        private String payeeName;

        @NotBlank(message = "accountNumber is required")
        @Size(max = 50)
        private String accountNumber;

        @NotBlank(message = "bankCode is required")
        @Size(max = 50)
        private String bankCode;

        @Size(max = 255)
        private String bankName;

        @Size(max = 100)
        private String nickname;

        @NotBlank(message = "currency is required")
        @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO 4217 code")
        private String currency;
    }

    /**
     * Request body for updating an existing payee.
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UpdatePayeeRequest {

        @Size(max = 255)
        private String payeeName;

        @Size(max = 50)
        private String accountNumber;

        @Size(max = 50)
        private String bankCode;

        @Size(max = 255)
        private String bankName;

        @Size(max = 100)
        private String nickname;

        @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO 4217 code")
        private String currency;
    }

    /**
     * Response body representing a payee.
     */
    @Value
    @Builder
    public static class PayeeResponse {
        String externalId;
        String payeeName;
        String accountNumber;
        String bankCode;
        String bankName;
        String nickname;
        String currency;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
    }
}
```