package com.banking.communityaccountservice.controller;

import com.banking.communityaccountservice.service.CommunityAccountService;
import com.banking.communityaccountservice.dto.CommunityCreateAccountRequest;
import com.banking.communityaccountservice.dto.CommunityAccountResponse;
import com.banking.communityaccountservice.dto.CommunityClaimResponse;
import com.banking.communityaccountservice.dto.CommunityDescriptionUpdateRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

/**
 * REST controller for community account operations such as creating a group,
 * joining a private group with a code, leaving a group, and transferring funds.
 */
@RestController
@RequestMapping("/api/v1/community-accounts")
@Slf4j
@RequiredArgsConstructor
public class CommunityAccountController {

    private final CommunityAccountService communityAccountService;

    @PostMapping("/community/create")
    public ResponseEntity<CommunityAccountResponse> createAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CommunityCreateAccountRequest request) {

        String creatorCustomerId = customerId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(communityAccountService.createAccount(creatorCustomerId, request));
    }

    // ADDED: account-number lookup returns the account users can review before joining.
    @GetMapping("/{accountNumber}")
    public ResponseEntity<CommunityAccountResponse> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(communityAccountService.getAccount(accountNumber));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(communityAccountService.getBalance(accountNumber));
    }


    @PutMapping("/{accountNumber}/block")
    public ResponseEntity<String> blockAccount(@PathVariable String accountNumber) {
        communityAccountService.blockAccount(accountNumber);
        return ResponseEntity.ok("Community account blocked successfully");
    }

    @PutMapping("/{accountNumber}/unblock")
    public ResponseEntity<String> unblockAccount(@PathVariable String accountNumber) {
        communityAccountService.unblockAccount(accountNumber);
        return ResponseEntity.ok("Community account unblocked successfully");
    }

    @PutMapping("/{accountNumber}/deduct")
    public ResponseEntity<String> deductBalance(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        communityAccountService.deductBalance(accountNumber, amount);
        return ResponseEntity.ok("Balance deducted successfully");
    }

    @PutMapping("/{accountNumber}/lock")
    public ResponseEntity<String> lockAccount(
        @PathVariable String accountNumber) {
            communityAccountService.lockAccount(accountNumber);
            return ResponseEntity.ok("Account has been locked. \nClaims will be prohibited until the account is unlocked");
        }


    @PutMapping("/{accountNumber}/credit")
    public ResponseEntity<String> creditBalance(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        communityAccountService.creditBalance(accountNumber, amount);
        return ResponseEntity.ok("Balance has been credited successfully");
    }

    @PutMapping("/{accountNumber}/claim")
    public ResponseEntity<CommunityClaimResponse> claimFunds(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam BigDecimal amount) {
        String customerId = customerId(jwt);
        return ResponseEntity.ok(communityAccountService.claimFunds(accountNumber, customerId, amount));
    }

    // ADDED: only the creator may update the group description and rules.
    @PutMapping("/{accountNumber}/description")
    public ResponseEntity<CommunityAccountResponse> updateDescription(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CommunityDescriptionUpdateRequest request) {
        String customerId = customerId(jwt);
        return ResponseEntity.ok(communityAccountService.updateDescription(accountNumber, customerId, request));
    }

    private String customerId(Jwt jwt) {
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

