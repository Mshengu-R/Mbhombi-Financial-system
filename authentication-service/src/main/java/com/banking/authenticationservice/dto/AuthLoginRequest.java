package com.banking.authenticationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthLoginRequest {
    
    @Email(message = "Please enter a valid email")
    @NotBlank( message = "Email is required")
    private String email;
    
    @NotBlank( message = "Please enter the correct password")
    private String password;
   
}
