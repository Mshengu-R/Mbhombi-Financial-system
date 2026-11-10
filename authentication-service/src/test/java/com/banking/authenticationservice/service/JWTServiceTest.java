package com.banking.authenticationservice.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JWTServiceTest {

    @Test
    void generatedTokenAuthenticatesOnlyItsSubject() {
        String secret = Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)
        );
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        JWTService jwtService = new JWTService(secret, Duration.ofMinutes(15), clock);
        UserDetails correctUser = User.withUsername("customer")
                .password("encoded-password")
                .roles("CUSTOMER")
                .build();
        UserDetails differentUser = User.withUsername("another-customer")
                .password("encoded-password")
                .roles("CUSTOMER")
                .build();

        String token = jwtService.generateToken("customer", "customer-id", "CUSTOMER");

        assertTrue(jwtService.validateToken(token, correctUser));
        assertFalse(jwtService.validateToken(token, differentUser));
    }
}
