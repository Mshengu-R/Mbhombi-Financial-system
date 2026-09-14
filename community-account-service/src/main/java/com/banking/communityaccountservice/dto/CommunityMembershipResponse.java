package com.banking.communityaccountservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityMembershipResponse {
    private String id;
    private String communityId;
    private String customerId;
    private String name;
    private String creatorName;
    private String displayName;
    private String accountNumber;
    private String role;
    private boolean privateCommunity;
    private String status;
    private BigDecimal dailyTransactionLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
