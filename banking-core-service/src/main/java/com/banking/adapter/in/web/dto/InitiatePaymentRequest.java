package com.banking.adapter.in.web.dto;

import com.banking.domain.model.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InitiatePaymentRequest {

    @NotBlank
    private String sourceAccountExternalId;

    @NotBlank
    private String targetAccountExternalId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotNull
    private PaymentType paymentType;

    private String description;
}
