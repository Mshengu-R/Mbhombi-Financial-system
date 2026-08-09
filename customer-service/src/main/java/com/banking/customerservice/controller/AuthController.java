package com.banking.customerservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.customerservice.dto.CustomerCreateRequest;
import com.banking.customerservice.dto.CustomerResponse;
import com.banking.customerservice.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final CustomerService customerService;

    @PostMapping("/sign-up")
    public ResponseEntity<CustomerResponse> createAccount(@Valid @RequestBody CustomerCreateRequest request) {
        log.info("Received profile creation request for email={}", request.getEmail());
        CustomerResponse response = customerService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<String> signIn() {
        return ResponseEntity.ok("Signed in successfully");
    }
}
