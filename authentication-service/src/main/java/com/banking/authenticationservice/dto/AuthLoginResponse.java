package com.banking.authenticationservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

public class AuthLoginResponse {


    private String id;

    private String email;

    private String phone;

    private String username;
    
    private BigDecimal idno;

    private String title;

    private String firstName;
    
    private String secondName;
    
    private String name;

    private String surname;

    private Date dateOfBirth;

    private String role;

    private String ethnicity;

    private String gender;

    private String country;

    private String Language;

    private ArrayList<String> homeAddress = new ArrayList<>();

    private ArrayList<String> otherAddress = new ArrayList<>();
  
    private String password;

    private String confirmPassword;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
