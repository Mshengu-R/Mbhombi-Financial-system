package com.banking.accountservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class AccountResponse {
    
    private String id;

    private String accountNumber;

    private String email;

    private String bankName;
    
    private String phone;

    private String accountHolderName;

    private AccountType accountType;

    private AccountStatus status;

    private BigDecimal balance;

    private String Role;

    private BigDecimal dailyTransactionLimit;

    private LocalDateTime createdAt;
}
