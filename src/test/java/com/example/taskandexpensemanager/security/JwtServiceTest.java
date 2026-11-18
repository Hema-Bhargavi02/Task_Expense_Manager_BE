package com.example.taskandexpensemanager.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.taskandexpensemanager.entity.UserEntity;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

class JwtServiceTest {

    // Test-only HMAC key (base64, >= 32 bytes when decoded); not used in production.
    private static final String SECRET
            = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private SecretKey secretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Test
    void tokenExpiresAfterTheConfiguredNumberOfMinutes() {

        SecretKey key = secretKey();

        JwtEncoder encoder = new NimbusJwtEncoder(
                new ImmutableSecret<>(key)
        );

        // 1 minute, as set by app.jwt.expiration-minutes.
        JwtService jwtService = new JwtService(encoder, 1);

        UserEntity user = new UserEntity();
        user.setUserId("alice");
        ReflectionTestUtils.setField(user, "id", 1L);

        String token = jwtService.generateToken(user);

        Jwt decoded = NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build()
                .decode(token);

        long lifetimeSeconds
                = decoded.getExpiresAt().getEpochSecond()
                - decoded.getIssuedAt().getEpochSecond();

        assertEquals(60, lifetimeSeconds);
        assertEquals("alice", decoded.getSubject());
    }

    @Test
    void theDefaultIsStillTwentyFourHours() {

        SecretKey key = secretKey();

        JwtEncoder encoder = new NimbusJwtEncoder(
                new ImmutableSecret<>(key)
        );

        // 1440 minutes is the @Value default.
        JwtService jwtService = new JwtService(encoder, 1440);

        UserEntity user = new UserEntity();
        user.setUserId("bob");
        ReflectionTestUtils.setField(user, "id", 2L);

        Jwt decoded = NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build()
                .decode(jwtService.generateToken(user));

        long lifetimeSeconds
                = decoded.getExpiresAt().getEpochSecond()
                - decoded.getIssuedAt().getEpochSecond();

        assertEquals(24 * 60 * 60, lifetimeSeconds);
    }
}
