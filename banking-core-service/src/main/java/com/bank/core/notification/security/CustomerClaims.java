package com.bank.core.notification.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/** Resolves the authenticated retail banking customer from the bearer token's {@code customerId} claim. */
public final class CustomerClaims {

    public static final String CUSTOMER_ID = "customerId";

    private CustomerClaims() {
    }

    public static String customerId(Jwt jwt) {
        String customerId = jwt.getClaimAsString(CUSTOMER_ID);
        return customerId == null || customerId.isBlank() ? null : customerId;
    }

    public static AuthorizationManager<RequestAuthorizationContext> requireCustomerId() {
        return (authentication, context) -> new AuthorizationDecision(hasCustomerId(authentication.get()));
    }

    private static boolean hasCustomerId(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt
                && customerId(jwt) != null;
    }
}
