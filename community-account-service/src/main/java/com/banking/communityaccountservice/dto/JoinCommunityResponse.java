package com.banking.communityaccountservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinCommunityResponse {

    private String id;

    private String communityId;

    private String customerId;

    private String displayName;

    private String accountNumber;

    private String bankName;

    private String role;

    private String memberName;

    // private boolean privateCommunity;

    private String status;

    private BigDecimal dailyTransactionLimit;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
