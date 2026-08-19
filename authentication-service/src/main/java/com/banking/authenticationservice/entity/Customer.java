package com.banking.authenticationservice.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "customer_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

 
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = true)
    private String secondName;
    
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;
    
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String idNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerTitle title;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

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

@ElementCollection
@CollectionTable(
    name = "customer_home_addresses",
    joinColumns = @JoinColumn(name = "customer_id")
)
@Column(name = "address")
private List<String> homeAddress;


@ElementCollection
@CollectionTable(
    name = "customer_other_addresses",
    joinColumns = @JoinColumn(name = "customer_id")
)
@Column(name = "address")
private List<String> otherAddress;

@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
@Column(nullable = false)
private String password;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
}
