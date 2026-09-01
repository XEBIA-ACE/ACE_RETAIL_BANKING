package com.banking.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplyForLoanRequest {

    @NotBlank
    private String customerExternalId;

    @NotNull
    @DecimalMin("1.00")
    private BigDecimal principal;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal interestRate;

    @NotNull
    @Min(1)
    private Integer termMonths;

    @NotBlank
    private String currency;
}
