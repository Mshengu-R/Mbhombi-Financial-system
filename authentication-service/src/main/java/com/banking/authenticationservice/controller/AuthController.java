package com.banking.authenticationservice.controller;

// import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.banking.authenticationservice.entity.Customer;
import com.banking.authenticationservice.entity.Consent;
import com.banking.authenticationservice.dto.ConsentRequest;
import com.banking.authenticationservice.repository.UserCredentialsRepository;
import com.banking.authenticationservice.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@CrossOrigin("http://localhost:3000")
public class AuthController {
    
    private final AuthService authService;
    private final UserCredentialsRepository customerRepository;

    public AuthController(AuthService authService, UserCredentialsRepository customerRepository) {
        this.authService = authService;
        this.customerRepository = customerRepository;
    }

@PostMapping("/register")
public ResponseEntity<String> addNewUser(@Valid @RequestBody Customer user){
    return ResponseEntity.status(HttpStatus.CREATED)
    .body(authService.saveUser(user));
}
 
@PostMapping("/login")
public ResponseEntity<String> login(@Valid @RequestBody Customer user) {
return ResponseEntity.ok( authService.authenticate(user));
}


@PostMapping("/consents")
public Consent recordConsent(@AuthenticationPrincipal UserDetails principal,
                 @Valid @RequestBody ConsentRequest request) {
    Customer customer = customerRepository.findByUsername(principal.getUsername())
        .orElseThrow(() -> new IllegalStateException("Authenticated customer was not found"));
    return authService.recordConsent(customer, request);
}


    // yet to be implimented
   @PostMapping("/logout")
   public ResponseEntity<String> logout() {
    return ResponseEntity.ok("Logged out successfully");
   }
    

}
