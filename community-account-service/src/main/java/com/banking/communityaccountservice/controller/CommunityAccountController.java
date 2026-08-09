package com.banking.communityaccountservice.controller;

import com.banking.communityaccountservice.service.CommunityAccountService;
import com.banking.communityaccountservice.dto.CommunityCreateAccountRequest;
import com.banking.communityaccountservice.dto.CommunityAccountResponse;

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

    @PostMapping
    public ResponseEntity<CommunityAccountResponse> createAccount(
        @Valid @RequestBody CommunityCreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(communityAccountService.createAccount(request));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<CommunityAccountResponse> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(communityAccountService.getAccount(accountNumber));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(communityAccountService.getBalance(accountNumber));
    }

    @PutMapping("/{accountNumber}/join")
    public ResponseEntity<CommunityAccountResponse> joinCommunity(
            @PathVariable @Valid String accountNumber,
            @RequestParam String memberName,
            @RequestParam(required = false) String joinCode) {
        return ResponseEntity.ok(communityAccountService.joinCommunity(accountNumber, memberName, joinCode));
    }

    @PutMapping("/{accountNumber}/leave")
    public ResponseEntity<CommunityAccountResponse> leaveCommunity(
            @PathVariable @Valid String accountNumber,
            @RequestParam String memberName) {
        return ResponseEntity.ok(communityAccountService.leaveCommunity(accountNumber, memberName));
    }

    @PutMapping("/{accountNumber}/block")
    public ResponseEntity<String> blockAccount(@PathVariable @Valid String accountNumber) {
        communityAccountService.blockAccount(accountNumber);
        return ResponseEntity.ok("Community account blocked successfully");
    }

    @PutMapping("/{accountNumber}/unblock")
    public ResponseEntity<String> unblockAccount(@PathVariable @Valid String accountNumber) {
        communityAccountService.unblockAccount(accountNumber);
        return ResponseEntity.ok("Community account unblocked successfully");
    }

    @PutMapping("/{accountNumber}/deduct")
    public ResponseEntity<String> deductBalance(
            @PathVariable @Valid String accountNumber,
            @RequestParam BigDecimal amount) {
        communityAccountService.deductBalance(accountNumber, amount);
        return ResponseEntity.ok("Balance deducted successfully");
    }

    @PutMapping("/{accountNumber}/credit")
    public ResponseEntity<String> creditBalance(
            @PathVariable @Valid String accountNumber,
            @RequestParam BigDecimal amount) {
        communityAccountService.creditBalance(accountNumber, amount);
        return ResponseEntity.ok("Balance has been credited successfully");
    }

    @PutMapping("/{accountNumber}/claim")
    public ResponseEntity<CommunityAccountResponse> claimFunds(
            @PathVariable @Valid String accountNumber,
            @RequestParam String memberName,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(communityAccountService.claimFunds(accountNumber, memberName, amount));
    }
}

