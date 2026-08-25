package com.banking.communityaccountservice.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommunityClaimResponse {

    // ADDED: makes the amount paid and remaining pool explicit to the caller.
    private String accountNumber;
    private String customerId;
    private BigDecimal claimedAmount;
    private BigDecimal remainingBalance;
}