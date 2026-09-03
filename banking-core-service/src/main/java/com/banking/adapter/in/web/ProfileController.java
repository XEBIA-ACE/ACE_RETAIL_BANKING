```java
package com.banking.adapter.in.web;

import com.banking.adapter.in.web.dto.ProfileResponse;
import com.banking.application.port.in.ProfileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the read-only profile endpoint for authenticated customers.
 * The customer's identity is resolved exclusively from the security context to prevent
 * horizontal privilege escalation.
 */
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Customer profile operations")
public class ProfileController {

    private final ProfileUseCase profileUseCase;

    /**
     * Returns the authenticated customer's profile data.
     *
     * @param authentication the Spring Security authentication object carrying the principal
     * @return HTTP 200 with {@link ProfileResponse}, or HTTP 404 if the customer is not found
     */
    @GetMapping
    @Operation(summary = "Get authenticated customer profile")
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        String externalId = authentication.getName();
        ProfileResponse response = profileUseCase.getProfile(externalId);
        return ResponseEntity.ok(response);
    }
}
```