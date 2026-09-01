package com.banking.adapter.in.web;

import com.banking.adapter.in.web.dto.InitiatePaymentRequest;
import com.banking.adapter.in.web.dto.PaymentResponse;
import com.banking.application.port.in.PaymentUseCase;
import com.banking.domain.model.Payment;
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
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment handling")
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    @PostMapping
    @Operation(summary = "Initiate a payment")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request) {
        Payment payment = paymentUseCase.initiatePayment(
                new PaymentUseCase.InitiatePaymentCommand(
                        request.getSourceAccountExternalId(),
                        request.getTargetAccountExternalId(),
                        request.getAmount(),
                        request.getCurrency(),
                        request.getPaymentType(),
                        request.getDescription()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(payment));
    }

    @PostMapping("/{externalId}/process")
    @Operation(summary = "Process a pending payment")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable String externalId) {
        return ResponseEntity.ok(toResponse(paymentUseCase.processPayment(externalId)));
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Get payment by external ID")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String externalId) {
        return paymentUseCase.findPaymentById(externalId)
                .map(p -> ResponseEntity.ok(toResponse(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List payments by account (paginated)")
    public ResponseEntity<List<PaymentResponse>> listPayments(
            @RequestParam String accountExternalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<PaymentResponse> payments =
                paymentUseCase.listPaymentsByAccount(accountExternalId, page, size)
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(payments);
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .externalId(payment.getExternalId())
                .sourceAccount(payment.getSourceAccount())
                .targetAccount(payment.getTargetAccount())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentType(payment.getPaymentType().name())
                .description(payment.getDescription())
                .status(payment.getStatus().name())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
