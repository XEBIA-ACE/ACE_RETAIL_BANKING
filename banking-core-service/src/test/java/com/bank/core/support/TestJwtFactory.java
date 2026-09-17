package com.bank.core.support;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Date;

public final class TestJwtFactory {

    public static final String SECRET =
            "test-secret-key-for-banking-core-service-must-be-at-least-64-bytes-long-123456789";

    private TestJwtFactory() {
    }

    public static String tokenForCustomer(String customerId) {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("test-user")
                .claim("customerId", customerId)
                .issuer("test")
                .expirationTime(new Date(System.currentTimeMillis() + 300_000))
                .build();
        try {
            SignedJWT jwt = new SignedJWT(new com.nimbusds.jose.JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            return jwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static JWTClaimsSet parse(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet();
        } catch (ParseException exception) {
            throw new IllegalArgumentException(exception);
        }
    }
}
