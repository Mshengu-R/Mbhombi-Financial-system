package com.banking.customerservice.entity;

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
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

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
    private CustomerTitle title;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private Date dateOfBirth;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerEthnicity ethnicity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerGender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerCountry country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerLanguage customerLanguage;

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

