package com.banking.customerservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

import com.banking.customerservice.entity.CustomerCountry;
import com.banking.customerservice.entity.CustomerEthnicity;
import com.banking.customerservice.entity.CustomerGender;
import com.banking.customerservice.entity.CustomerTitle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private String id;
    private String email;
    private String phone;
    private BigDecimal idno;
    private CustomerTitle title;
    private String name;
    private String surname;
    private Date dateOfBirth;
    private String role;
    private String customerLanguage;
    private CustomerEthnicity ethnicity;
    private CustomerGender gender;
    private CustomerCountry country;
    private ArrayList<String> homeAdress;
    private ArrayList<String> otherAdress;
    private String password;
    private String confirmPassword;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

