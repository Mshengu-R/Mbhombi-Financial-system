package com.banking.accountservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;

    @NotNull(message = "UserId is required")
    private String customerId;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Account type is required")
    private String accountType;

    @NotNull(message = "Pin is required")
    @DecimalMin(value = "0", inclusive = true, message = "Pin should be a valid decimal number")
    private BigDecimal pin;

    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "50", inclusive = true, message = "Initial deposit should be greater than or equal to R50")
    @Positive(message = "Initial deposit must be positive")
    private BigDecimal initialDeposit;

}
