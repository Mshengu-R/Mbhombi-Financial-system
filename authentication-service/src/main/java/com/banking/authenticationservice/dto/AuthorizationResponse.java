package com.banking.authenticationservice.dto;

import java.time.LocalDate;
import java.util.ArrayList;

import jakarta.validation.constraints.NotBlank;

public class AuthorizationResponse {
        
    private String email;

    private String username;

    private String phone;

    private String idno;

    private String firstName;

    private String secondName;

    private String title;

    private String role;
    
    private String surname;

    private String name;

    private String customerLanguage;

    private String gender;

    private LocalDate dateOfBirth;

    private ArrayList<String> homeAdress = new ArrayList<>();

    private String ethnicity;

    private String country;

    private String password;

}
