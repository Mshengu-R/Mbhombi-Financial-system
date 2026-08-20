package com.banking.communityaccountservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommunityMembershipCreate {


    @NotBlank(message = "Community id is required")
    private String communityId;

    @NotBlank(message = "Customer id is required")
    private String customerId;

    @NotBlank(message = "Display name is required")
    private String displayName;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    private boolean privateCommunity;

    private BigDecimal dailyTransactionLimit;

}
