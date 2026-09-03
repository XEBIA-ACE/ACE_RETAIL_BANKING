```java
package com.bank.core.account.service;

import com.bank.core.account.dto.DashboardResponse;

public interface AccountService {

    /**
     * Retrieves a consolidated dashboard view for the given customer,
     * including all linked accounts with masked account numbers and aria labels.
     *
     * @param customerId the internal customer identifier
     * @return a {@link DashboardResponse} containing account summaries or an empty-state response
     */
    DashboardResponse getDashboard(Long customerId);
}
```