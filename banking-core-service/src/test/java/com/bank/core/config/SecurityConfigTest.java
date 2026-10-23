package com.bank.core.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bank.core.support.TestJwtFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** [US-028-AC4] Customer tokens are verified with a strong signing secret only. */
class SecurityConfigTest {

    /** [US-028-AC4] Startup fails when the JWT secret is missing or shorter than 32 bytes. */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"too-short-secret"})
    void us028Ac4_rejectsWeakJwtSecret(String secret) {
        assertThatThrownBy(() -> new SecurityConfig().jwtDecoder(new SecurityConfig.JwtProperties(secret)))
                .isInstanceOf(IllegalStateException.class);
    }

    /** [US-028-AC4] A sufficiently long secret produces a decoder that accepts tokens it signed. */
    @Test
    void us028Ac4_strongSecretDecodesCustomerToken() {
        var decoder = new SecurityConfig().jwtDecoder(new SecurityConfig.JwtProperties(TestJwtFactory.SECRET));

        assertThat(decoder.decode(TestJwtFactory.tokenForCustomer("cust-a")).getClaimAsString("customerId"))
                .isEqualTo("cust-a");
    }
}
