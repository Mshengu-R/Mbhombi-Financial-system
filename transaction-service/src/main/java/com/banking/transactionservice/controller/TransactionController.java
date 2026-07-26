package com.banking.transactionservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import com.banking.transactionservice.dto.TransactionRequest;
import com.banking.transactionservice.dto.TransactionResponse;
import com.banking.transactionservice.service.TransactionService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/transactions")
@Slf4j
public class TransactionController {
    
    private final TransactionService transactionService;

    // Constructor injection for TransactionService
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Endpoint to initiate a transfer transaction
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
        @Valid @RequestBody TransactionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(transactionService.transfer(request));
    }

    // Endpoint to retrieve a specific transaction by its ID
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(
        @PathVariable String transactionId ) {

        return ResponseEntity.ok(transactionService.getTransaction(transactionId));

    }

    // Endpoint to retrieve the transaction history for a specific account
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
        @PathVariable String accountNumber ) {

        return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber));

    }

    // Endpoint to verify the OTP for a specific transaction
    @PostMapping("/{transactionId}/verify")
    public ResponseEntity<TransactionResponse> verifyOTP(
        @PathVariable String transactionId,
        @RequestParam String otp) {

            log.info("OTP verification request - transaction: {}", transactionId);

            return ResponseEntity.ok(transactionService.verifyOTP(transactionId, otp));
            
        }




}
