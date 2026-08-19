package com.banking.authenticationservice.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banking.authenticationservice.entity.Customer;
import com.banking.authenticationservice.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    
    private final AuthService authService;

@PostMapping("/register")
public String addNewUser(@RequestBody Customer user){
    return authService.saveUser(user);
}
 
@PostMapping("/login")

public String login(@RequestBody Customer user) {
return authService.authenticate(user);
}

    // yet to be implimented
   @PostMapping("/logout")
public ResponseEntity<String> logout() {
    return ResponseEntity.ok("Logged out successfully");
}
    // @GetMapping("/token")
    // public String getToken(Customer userCredentials){
    //     return authService.generateToken(userCredentials.getName());
    // }

    // @GetMapping("/validate")
    // public String validateToken(@RequestParam("token") String token){
    //     authService.validateToken(token);
    //     return "Token is valid";
    // }
    

}
