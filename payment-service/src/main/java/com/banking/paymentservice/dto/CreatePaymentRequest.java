package com.banking.paymentservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banking.paymentservice.entity.PaymentStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
   

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount should be positive")
    private BigDecimal amount;


    private String currency;

    private PaymentStatus status;

    private String description;

    private String failureReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
}
