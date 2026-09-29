package com.banking.authenticationservice.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    // private String name;

    private String customerLanguage;

    private String gender;

    private String status;

    private LocalDate dateOfBirth;

    private List<String> homeAdress = new ArrayList<>();

    private String ethnicity;

    private String country;

    private String password;

}
