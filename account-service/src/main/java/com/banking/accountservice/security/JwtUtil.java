package com.banking.accountservice.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class JwtUtil {

    public String customerId(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authenticated customer is required");
        }

        try {
            String customerId = jwt.getClaimAsString("customerId");
            if (customerId == null || customerId.isBlank()) {
                throw new IllegalArgumentException("customerId claim is missing");
            }
            return customerId.trim();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "JWT does not contain a valid customer ID", ex);
        }
    }
}
