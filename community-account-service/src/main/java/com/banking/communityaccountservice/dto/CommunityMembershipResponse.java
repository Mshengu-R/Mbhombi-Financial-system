package com.banking.communityaccountservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banking.communityaccountservice.entity.CommunityAccountStatus;
import com.banking.communityaccountservice.entity.CommunityAccountType;


public class CommunityMembershipResponse {
    
    private String id;
    private String customerId;
    private String name;
    private String creatorName;
    private String description;
    private String rulesDescription;
    private String accountNumber;
    private String bankName;
    private CommunityAccountType accountType;
    private boolean privateCommunity;
    private String joinCode;
    private CommunityAccountStatus status;
    private String otherType;
    private BigDecimal balance;
    private BigDecimal dailyTransactionLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
