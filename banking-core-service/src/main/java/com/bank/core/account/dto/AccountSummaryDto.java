```java
package com.bank.core.account.dto;

import com.bank.core.account.domain.AccountStatus;
import com.bank.core.account.domain.AccountType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class AccountSummaryDto {

    String externalId;
    AccountType accountType;
    String accountNumber;
    BigDecimal balance;
    String currency;
    AccountStatus status;
    String nickname;
}
```