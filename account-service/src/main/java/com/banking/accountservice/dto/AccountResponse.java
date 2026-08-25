package com.banking.accountservice.dto;

import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private String id;

    private String accountNumber;

    private String customerId;

    private String phone;
    // added in the database so if the request comes without it it will break

    private String bankName;

    private String currency;

    private String accountHolderName;

    private AccountType accountType;

    private AccountStatus status;

    private BigDecimal balance;

    private BigDecimal pin;

    private BigDecimal dailyTransactionLimit;

    private LocalDateTime createdAt;
}
