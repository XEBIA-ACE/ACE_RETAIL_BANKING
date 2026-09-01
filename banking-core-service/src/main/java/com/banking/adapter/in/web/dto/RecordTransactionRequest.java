package com.banking.adapter.in.web.dto;

import com.banking.domain.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecordTransactionRequest {

    @NotBlank
    private String accountExternalId;

    @NotNull
    private TransactionType transactionType;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String currency;

    private String description;
    private String reference;
}
