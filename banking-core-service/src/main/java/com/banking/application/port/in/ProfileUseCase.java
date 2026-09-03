```java
package com.banking.application.port.in;

import com.banking.adapter.in.web.dto.ProfileResponse;

/**
 * Inbound port for retrieving a customer's profile.
 * Follows the hexagonal architecture pattern established by existing use case interfaces.
 */
public interface ProfileUseCase {

    /**
     * Retrieves the profile for the customer identified by the given external ID.
     *
     * @param customerExternalId the external identifier of the authenticated customer
     * @return a {@link ProfileResponse} containing the customer's profile data
     * @throws com.banking.domain.exception.ResourceNotFoundException if no customer exists for the given ID
     */
    ProfileResponse getProfile(String customerExternalId);
}
```