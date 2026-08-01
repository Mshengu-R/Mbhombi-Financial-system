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

    private String accountNumber;

    // private String email;

    private String bankName;

    // private String phone;

    private String accountHolderName;

    private String customerId;

    private String otherType;

    private CommunityAccountType accountType;

    private CommunityAccountStatus status;

    private BigDecimal balance;

    private BigDecimal dailyTransactionLimit;

    private LocalDateTime createdAt;
}
