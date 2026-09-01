package com.banking.adapter.in.web.dto;

import com.banking.domain.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OpenAccountRequest {

    @NotBlank
    private String customerExternalId;

    @NotNull
    private AccountType accountType;

    @NotBlank
    private String currency;
}
