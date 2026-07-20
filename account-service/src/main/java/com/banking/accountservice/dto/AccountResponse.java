package com.banking.accountservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class AccountResponse {
    

    private String accountNumber;

    private String accountType;

    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "valid email format should be something like example@domain.com")
    private String email;

    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;


    private BigDecimal initialDeposit;


}
