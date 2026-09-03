```java
package com.bank.core.account.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class AccountSummaryDto {

    String accountId;
    String accountTypeName;
    String maskedAccountNumber;
    BigDecimal currentBalance;
    String currencyCode;
    String ariaLabel;
}
```