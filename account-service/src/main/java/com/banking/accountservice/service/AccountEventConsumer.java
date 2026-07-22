package com.banking.accountservice.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccountEventConsumer {

    private final AccountService accountService;

    // CREDITING THE RECIVER
    @KafkaListener(topics = "transaction.completed")
    public void consumeTransactionCompleted(
        @Payload Map<String, Object> payload) {

            try {
    
                String reciverAccount = (String) payload.get("recieverAccountNumber");
                BigDecimal amount = new BigDecimal(payload.get("amount").toString());

                log.info("Crediting account: {} amount: {}", reciverAccount, amount);
                accountService.creditBalance(reciverAccount, amount);


            } catch (Exception e) {
                // TODO: handle exception
                log.error("Error crediting account: {}", e.getMessage());
            }
    }

    // Consume fraud
    @KafkaListener(topics = "fraud.detected")
    public void consumerFraudDetected(
        @Payload Map<String, Object> payload
    ){
        try {
            
            String accountNumber = (String) payload.get("accountNumber");
            log.warn("Fraud detected - blocking account: {}", accountNumber);

            accountService.blockAccount(accountNumber);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}