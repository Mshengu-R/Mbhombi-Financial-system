package com.banking.authenticationservice.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {

    private final SecretKey signingKey;
    private final Duration accessTokenExpiration;
    private final Clock clock;

    public JWTService(
            @Value("${security.jwt.secret}") String base64Secret,
            @Value("${security.jwt.access-token-expiration}")
            Duration accessTokenExpiration
    ) {
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT secret must not be blank"
            );
        }

        this.accessTokenExpiration = Objects.requireNonNull(
                accessTokenExpiration,
                "JWT access-token expiration must not be null"
        );

        if (accessTokenExpiration.isZero()
                || accessTokenExpiration.isNegative()) {
            throw new IllegalArgumentException(
                    "JWT access-token expiration must be greater than zero"
            );
        }

        byte[] decodedSecret;

        try {
            decodedSecret = Decoders.BASE64.decode(base64Secret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "JWT secret must be valid Base64",
                    exception
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(decodedSecret);
        this.clock = Clock.systemUTC();
    }

    public String generateToken(String username) {
        return generateToken(Map.of(), username);
    }

    public String generateToken(
            Map<String, Object> additionalClaims,
            String username
    ) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT subject must not be blank"
            );
        }

        Objects.requireNonNull(
                additionalClaims,
                "Additional claims must not be null"
        );

        if (additionalClaims.containsKey(Claims.SUBJECT)
                || additionalClaims.containsKey(Claims.ISSUED_AT)
                || additionalClaims.containsKey(Claims.EXPIRATION)) {
            throw new IllegalArgumentException(
                    "Additional claims must not override subject, "
                            + "issued-at, or expiration claims"
            );
        }

        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(accessTokenExpiration);

        return Jwts.builder()
                .claims(additionalClaims)
                .subject(username)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimResolver
    ) {
        Objects.requireNonNull(
                claimResolver,
                "Claim resolver must not be null"
        );

        Claims claims = extractAllClaims(token);

        return claimResolver.apply(claims);
    }

    public boolean validateToken(
            String token,
            UserDetails userDetails
    ) {
        if (token == null
                || token.isBlank()
                || userDetails == null) {
            return false;
        }

        try {
            String username = extractUsername(token);

            return username != null
                    && username.equals(userDetails.getUsername())
                    && userDetails.isEnabled()
                    && userDetails.isAccountNonExpired()
                    && userDetails.isAccountNonLocked()
                    && userDetails.isCredentialsNonExpired()
                    && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);

        return expiration.before(
                Date.from(clock.instant())
        );
    }

    private Claims extractAllClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT must not be blank"
            );
        }

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}