package com.banking.customerservice.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateRequest {

    @NotBlank(message = "User email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotNull(message = "Identity number is required")
    @Positive(message = "Identity number should be positive")
    private BigDecimal idno;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Surname is required")
    private String surname;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Language is required")
    private String customerLanguage;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotNull(message = "Date of birth is required")
    private Date dateOfBirth;

    private ArrayList<String> homeAdress = new ArrayList<>();

    @NotBlank(message = "Ethnicity is required")
    private String ethnicity;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    
}

