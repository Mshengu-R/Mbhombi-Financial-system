package com.banking.communityaccountservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "community_accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @JoinColumn(nullable = false, unique = true)
    private String customerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String creatorName;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String rulesDescription;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunityAccountType accountType;

    @Column(nullable = false)
    private boolean privateCommunity;

    @Column(nullable = false, length = 7)
    private String joinCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunityAccountStatus status;

    @Column(name = "other_type", nullable = true)
    private String otherType;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal dailyTransactionLimit;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
