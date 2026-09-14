package com.banking.customerservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.customerservice.dto.CustomerResponse;
import com.banking.customerservice.dto.CustomerUpdateRequest;
import com.banking.customerservice.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:3000")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me")
    public CustomerResponse getAccount(@AuthenticationPrincipal Jwt jwt) {
        return customerService.getAccount(customerId(jwt));
    }

    
    // @GetMapping("/me")
    // public CustomerResponse getAccount(@RequestHeader("customerId") UUID customerId) {
    //     return customerService.getAccount(customerId);
    // }

    @PatchMapping("/me")
    public CustomerResponse updateAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CustomerUpdateRequest request) {
        return customerService.updateAccount(customerId(jwt), request);
    }

    // @PatchMapping("/me")
    // public CustomerResponse updateAccountPartially(
    //      @AuthenticationPrincipal Jwt jwt,
    //         @Valid @RequestBody CustomerUpdateRequest request) {
    //     return customerService.updateAccountPartially(customerId(jwt), request);
    //     }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal Jwt jwt) {
        customerService.deleteAccount(customerId(jwt));
        return ResponseEntity.noContent().build();
    }


    private UUID customerId(Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Authenticated customer is required");
        }
        try {
            String customerId = jwt.getClaimAsString("customerId");
            if (customerId == null || customerId.isBlank()) {
                throw new IllegalArgumentException("customerId claim is missing");
            }
            return UUID.fromString(customerId);
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "JWT does not contain a valid customer ID", ex);
        }
    }
}
