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
@Table(name = "community_memberships")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @JoinColumn(nullable = false, unique = true)
    private String communityId;

    @Column(nullable = false)
    private String customerId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(nullable = false)
    private CommunityAccountUserRole role;

    @Column(nullable = false)
    private boolean privateCommunity;

    @Column(nullable = false, length = 7)
    private String joinCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunityAccountStatus status;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal dailyTransactionLimit;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
