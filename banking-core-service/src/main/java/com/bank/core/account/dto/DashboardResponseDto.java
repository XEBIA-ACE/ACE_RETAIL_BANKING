```java
package com.bank.core.account.dto;

import com.bank.core.account.domain.AccountType;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class DashboardResponseDto {

    String customerId;
    Map<AccountType, List<AccountSummaryDto>> accounts;
}
```