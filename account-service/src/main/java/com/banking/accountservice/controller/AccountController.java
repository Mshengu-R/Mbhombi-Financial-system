package com.banking.accountservice.controller;

import com.banking.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.dto.AccountResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/accounts")
@Slf4j
@RequiredArgsConstructor
public class AccountController {
    
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<String> createAccount(
        @Valid @RequestBody CreateAccountRequest request) {
        // log.info("Creating account for customer: {}", accountRequest.getCustomerId());
        // String accountId = accountService.createAccount(accountRequest);
     
        return ResponseEntity.status(HttpStatus.CREATED)
                    .body(accountService.createAccount(request));
    }


    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(
        @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccount(accountNumber));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(
        @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getBalance(accountNumber));
    }


    @PutMapping("/{accountNumber}/block")
    public ResponseEntity<String> blockAccount(
        @PathVariable String accountNumber) {

        accountService.blockAccount(accountNumber);
        return ResponseEntity.ok("Account blocked successfully");
    }

    @PutMapping("/{accountNumber}/unblock")
    public ResponseEntity<String> unblockAccount(
        @PathVariable String accountNumber) {

        accountService.unblockAccount(accountNumber);
        return ResponseEntity.ok("Account unblocked successfully");
    }

    // SAGA STEP 1
    @PutMapping("/{accountNumber}/deduct")
    public ResponseEntity<String> deductBalance(

         @PathVariable String accountNumber,
         @RequestParam BigDecimal amount) {

        accountService.deductBalance(accountNumber, amount);
        return ResponseEntity.ok("Balance deducted successfully");
    }

    // SAGA STEP 2
    @PutMapping("/{accountNumber}/credit")
    public ResponseEntity<String> creditBalance(

        @PathVariable String accountNumber,
        @RequestParam BigDecimal amount) {

            accountService.creditBalance(accountNumber, amount);
            return ResponseEntity.ok("Balance has been credited to the receiver successfully");
        }

    

        

}
