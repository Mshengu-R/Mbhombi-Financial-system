/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banking.authenticationservice.dto;

import com.banking.authenticationservice.entity.CustomerCountry;
import com.banking.authenticationservice.entity.CustomerEthnicity;
import com.banking.authenticationservice.entity.CustomerGender;
import com.banking.authenticationservice.entity.CustomerLanguage;
import com.banking.authenticationservice.entity.CustomerRole;
import com.banking.authenticationservice.entity.CustomerTitle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Dell
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationRequest {
    
    @NotBlank(message = "User email is required")
    private String email;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotNull(message = "Identity number is required")
    // @Positive(message = "Identity number should be positive")
    private String idno;

    @NotBlank(message = "First name is required")
    private String firstName;

    private String secondName;

    @NotBlank(message = "Title is required")
    private String title;

//    @NotBlank(message = "")
   private String role;

    
    @NotBlank(message = "Surname is required")
    private String surname;

    @NotBlank(message = "Please enter your names as they appear in your Id")
    private String name;

    @NotBlank(message = "Language is required")
    private String customerLanguage;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    private ArrayList<String> homeAdress = new ArrayList<>();

    @NotBlank(message = "Ethnicity is required")
    private String ethnicity;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Password is required")
    private String password;

//    @NotBlank(message = "Confirm password is required")
//    private String confirmPassword;
}
