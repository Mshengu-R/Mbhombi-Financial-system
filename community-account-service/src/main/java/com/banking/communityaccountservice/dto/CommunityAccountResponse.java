package com.banking.communityaccountservice.dto;

import com.banking.communityaccountservice.entity.CommunityAccountStatus;
import com.banking.communityaccountservice.entity.CommunityAccountType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityAccountResponse {

    private String id;
    private String name;
    private String creatorName;
    private String description;
    private String rulesDescription;
    // private String displayName;
    private String accountNumber;
    private String bankName;
    private String otherType;
    private CommunityAccountType accountType;
    // private boolean privateCommunity;
    private String joinCode;
    private CommunityAccountStatus status;
    private BigDecimal balance;
    private BigDecimal dailyTransactionLimit;
    private LocalDateTime createdAt;
}
