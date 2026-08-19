package com.banking.communityaccountservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public class CommunityMembershipCreate {


    @NotBlank(message = "Community id name is required")
    private String communityId;

    @NotBlank(message = "Customer id name is required")
    private String customerId;

    @NotBlank(message = "Display name is required")
    private String displayName;

    @NotBlank(message = "Community name is required")
    private String accountNumber;

    @NotBlank(message = "Community name is required")
    private String bankName;

    @NotBlank(message = "Community type is required")
    private boolean privateCommunity;

    @NotBlank(message = "Community name is required")
    private BigDecimal dailyTransactionLimit;

}
