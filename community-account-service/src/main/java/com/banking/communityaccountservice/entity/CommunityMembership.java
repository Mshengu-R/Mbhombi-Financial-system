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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "community_memberships", uniqueConstraints = {
    @jakarta.persistence.UniqueConstraint(name = "sa_membership_community_customer",
        columnNames = {"community_id", "customer_id"})
})
// @Table(name = "community_memberships")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "community_id", nullable = false)
    private String communityId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String accountNumber;

    @Column(name = "member_name", nullable = false)
    private String memberName;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunityMemberRole role;

    // @Column(nullable = false)
    // private boolean privateCommunity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunityMembershipStatus status;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal dailyTransactionLimit;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
