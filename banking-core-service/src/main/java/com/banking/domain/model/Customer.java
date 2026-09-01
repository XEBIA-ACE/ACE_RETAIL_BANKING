package com.banking.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Customer domain entity (pure domain object — no JPA annotations).
 */
@Value
@Builder(toBuilder = true)
public class Customer {

    Long id;
    String externalId;
    String firstName;
    String lastName;
    String email;
    String phone;
    CustomerStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
