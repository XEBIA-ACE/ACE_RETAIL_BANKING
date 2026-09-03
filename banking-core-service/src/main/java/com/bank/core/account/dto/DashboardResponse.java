```java
package com.bank.core.account.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class DashboardResponse {

    boolean hasAccounts;
    String emptyStateMessage;
    List<AccountSummaryDto> accounts;
}
```