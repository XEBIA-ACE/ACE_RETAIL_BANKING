package com.bank.core.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.core.notification.security.CustomerClaims;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** [US-028-AC4] The authenticated customer is resolved only from a valid, customer-bearing token. */
class NotificationHistoryAccessControlTest {

    private static Jwt jwt(String customerId) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "HS256").subject("test-user")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300));
        if (customerId != null) {
            builder.claim(CustomerClaims.CUSTOMER_ID, customerId);
        }
        return builder.build();
    }

    private static boolean granted(Authentication authentication) {
        Supplier<Authentication> supplier = () -> authentication;
        return CustomerClaims.requireCustomerId().check(supplier, null).isGranted();
    }

    /** [US-028-AC4] A blank customerId claim never identifies a customer. */
    @Test
    void us028Ac4_blankCustomerClaimIsTreatedAsMissing() {
        assertThat(CustomerClaims.customerId(jwt("cust-a"))).isEqualTo("cust-a");
        assertThat(CustomerClaims.customerId(jwt("   "))).isNull();
        assertThat(CustomerClaims.customerId(jwt(null))).isNull();
    }

    /** [US-028-AC4] Access requires an authenticated JWT principal carrying a customerId. */
    @Test
    void us028Ac4_accessRequiresAuthenticatedCustomerToken() {
        assertThat(granted(new JwtAuthenticationToken(jwt("cust-a"), List.of()))).isTrue();
        assertThat(granted(new JwtAuthenticationToken(jwt(null), List.of()))).isFalse();
        assertThat(granted(null)).isFalse();

        TestingAuthenticationToken nonJwt = new TestingAuthenticationToken("cust-a", "n/a");
        nonJwt.setAuthenticated(true);
        assertThat(granted(nonJwt)).isFalse();

        JwtAuthenticationToken unauthenticated = new JwtAuthenticationToken(jwt("cust-a"));
        unauthenticated.setAuthenticated(false);
        assertThat(granted(unauthenticated)).isFalse();
    }
}
