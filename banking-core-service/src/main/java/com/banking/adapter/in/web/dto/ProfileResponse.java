```java
package com.banking.adapter.in.web.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Outbound DTO representing a customer's read-only profile data.
 * Internal database IDs are never included; only {@code externalId} is exposed.
 */
@Value
@Builder
public class ProfileResponse {

    /** The customer's external (public) identifier. */
    String externalId;

    /** The customer's full name (firstName + " " + lastName). */
    String name;

    /** The customer's email address. */
    String email;

    /** The date and time the customer account was registered (ISO-8601). */
    LocalDateTime registrationDate;

    /** The current lifecycle status of the customer account (e.g., ACTIVE, INACTIVE). */
    String accountStatus;
}
```