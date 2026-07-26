package com.banking.transactionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.banking.transactionservice.entity.TransactionStatus;
import com.banking.transactionservice.entity.TransactionType;

// import jakarta.persistence.Column;
// import jakarta.persistence.Enumerated;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

    private String id;

    private String senderAccountNumber;
    
    private String recieverAccountNumber;

    private BigDecimal amount;

 
    private TransactionType type;

    private TransactionStatus status;

    // @Column(nullable = false)
    private String description;

    private String failureReason;

    private String referenceNumber;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime completedAt; // transaction completion

}
