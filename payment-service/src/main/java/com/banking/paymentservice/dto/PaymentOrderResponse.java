package com.banking.paymentservice.dto;

import java.math.BigDecimal;

import lombok.*;
import com.banking.paymentservice.entity.PaymentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PaymentOrderResponse {
    
    private String id;

    private String payFastOderId;
  
    private String payFastId;
   
    private String payFastKeyId;

    private String accountNumber;

    private BigDecimal amount;

    private String currency;

    private String status;

    private String description;

    private String failureReason;

    private String paymentUrl;

}
