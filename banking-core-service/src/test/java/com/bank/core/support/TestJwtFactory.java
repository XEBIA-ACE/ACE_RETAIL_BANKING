package com.bank.core.support;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public final class TestJwtFactory {

    public static final String SECRET =
            "test-secret-key-for-banking-core-service-must-be-at-least-64-bytes-long-123456789";

    private TestJwtFactory() {
    }

    public static String tokenForCustomer(String customerId) {
        return sign(baseClaims().claim("customerId", customerId).build());
    }

    public static String tokenWithScope(String scope) {
        return sign(baseClaims().subject("smart-notifications").claim("scope", scope).build());
    }

    public static String tokenWithoutCustomer() {
        return sign(baseClaims().build());
    }

    private static JWTClaimsSet.Builder baseClaims() {
        return new JWTClaimsSet.Builder()
                .subject("test-user")
                .issuer("test")
                .expirationTime(new Date(System.currentTimeMillis() + 300_000));
    }

    private static String sign(JWTClaimsSet claims) {
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(SECRET.getBytes(StandardCharsets.UTF_8)));
            return jwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
