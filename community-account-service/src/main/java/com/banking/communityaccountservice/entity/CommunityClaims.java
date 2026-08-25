// package com.banking.communityaccountservice.entity;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;

// @Entity
// @Table(name = "community_accounts_claims")
// @Data
// @AllArgsConstructor
// @NoArgsConstructor
// public class CommunityClaims {

//     @Id
//     @GeneratedValue(strategy = GenerationType.UUID)
//     private String id;
    
//     @Column(nullable = false)
//     private String communityId;

//     @Column(nullable = false)
//     private String customerId;

//     @Column(nullable = false)
//     private String communityAccount;

//     @Column(nullable = false)
//     private String claimantAccount;
//     // We are going to add those who allowed the claims to go through

//     @Column(nullable = false)
//     private BigDecimal amoutClaimed;

//     @Column(nullable = false)
//     private BigDecimal balance;
    
//     @Column(nullable = false)
//     private LocalDateTime claimDate;

    
// }
