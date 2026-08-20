package com.banking.communityaccountservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinCommunityRequest {
    
    @NotBlank(message = "Community Id is required")
    private String communityId;

    @NotBlank(message = "Customer id is required")
    private String customerId;

    // @NotBlank(message = "Display name is")
    private String displayName;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "Bank name is required")
    private String bankName;

    @NotNull(message = "Customer role is required")
    private String role;

    private boolean privateCommunity;

    private String status;

    private LocalDateTime createdAt;
}
