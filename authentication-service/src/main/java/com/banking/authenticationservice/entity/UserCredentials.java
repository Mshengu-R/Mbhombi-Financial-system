package com.banking.authenticationservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.GenerationType;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentials {

   @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false)
    private BigDecimal idno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserTitle title;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserEnthnicity ethnicity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserGender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserCountry country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserLanguage customerLanguage;

    @Column(nullable = false)
    private ArrayList<String> homeAddress = new ArrayList<>();

    @Column(nullable = false)
    private ArrayList<String> otherAddress = new ArrayList<>();
  
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String confirmPassword;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
}
