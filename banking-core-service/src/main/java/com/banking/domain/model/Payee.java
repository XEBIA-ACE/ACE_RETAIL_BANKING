```java
package com.banking.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Payee domain entity (pure domain object — no JPA annotations).
 */
@Value
@Builder(toBuilder = true)
public class Payee {

    Long id;
    String externalId;
    Long customerId;
    String payeeName;
    String accountNumber;
    String bankCode;
    String bankName;
    String nickname;
    String currency;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```