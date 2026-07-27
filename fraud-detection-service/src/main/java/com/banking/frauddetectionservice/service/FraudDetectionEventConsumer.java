package com.banking.frauddetectionservice.service;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionEventConsumer {
    
    private final FraudDetectionService fraudDetectionService;

    // listen to the transaction.initiated topics
@KafkaListener(topics = "transaction.initiated", groupId = "fraud-detection-group")
    public void consumeTransactionInitiated(
        @Payload Map<String, Object> payload) {

            log.info("Recieved transaction for fraud detection: {}", payload.get("transactionId"));

            try {
    
                // check transaction will be responsible for checking the transactions
                fraudDetectionService.checkTransaction(payload);

            } catch (Exception e) {
                // TODO: handle exception
                log.error("Server error", e.getMessage());
            
            }
    }
}
