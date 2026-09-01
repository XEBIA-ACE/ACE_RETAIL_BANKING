package com.banking.adapter.in.web.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class CustomerResponse {
    String externalId;
    String firstName;
    String lastName;
    String email;
    String phone;
    String status;
    LocalDateTime createdAt;
}
