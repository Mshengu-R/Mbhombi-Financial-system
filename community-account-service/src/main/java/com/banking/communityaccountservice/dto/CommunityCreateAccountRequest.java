package com.banking.communityaccountservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityCreateAccountRequest {

    @NotBlank(message = "Community name is required")
    private String name;

    @NotBlank(message = "Creator name is required")
    private String creatorName;

    // ADDED: identifies the customer who owns/creates this community account.
    @NotBlank(message = "Creator customer id is required")
    private String customerId;

    @NotBlank(message = "Community description is required")
    private String description;

    @NotBlank(message = "Community rules description is required")
    private String rulesDescription;

    @NotBlank(message = "Account type is required")
    private String accountType;

    // REMOVED as a required input: the service owns the bank name.
    private String bankName;

    @NotBlank(message = "Display name is required")
    private String displayName;

    private String otherType;

    private boolean privateCommunity;

    // @NotNull(message = "Customer's id is required to join the community")
    private String membersId;

    private String pin;

    // CHANGED: the creator supplies the six-digit code used by members to join.
    @NotBlank(message = "A six-digit join code is required")
    private String joinCode;

    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "50", inclusive = true, message = "Initial deposit should be greater than or equal to R50")
    @Positive(message = "Initial deposit must be positive")
    private BigDecimal initialDeposit;
}

