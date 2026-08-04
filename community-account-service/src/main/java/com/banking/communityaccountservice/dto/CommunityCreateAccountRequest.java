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

    @NotBlank(message = "Community description is required")
    private String description;

    @NotBlank(message = "Community rules description is required")
    private String rulesDescription;

    @NotBlank(message = "Account type is required")
    private String accountType;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Display name is required")
    private String displayName;

    private String otherType;

    private boolean privateCommunity;

    private String joinCode;

    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "50", inclusive = true, message = "Initial deposit should be greater than or equal to R50")
    @Positive(message = "Initial deposit must be positive")
    private BigDecimal initialDeposit;
}

