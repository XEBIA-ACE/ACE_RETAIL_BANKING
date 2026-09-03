```java
package com.bank.core.account.service;

import com.bank.core.account.dto.DashboardResponseDto;

public interface AccountService {

    DashboardResponseDto getAccountsByCustomer(String customerExternalId);
}
```