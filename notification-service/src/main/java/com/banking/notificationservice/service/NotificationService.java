package com.banking.notificationservice.service;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {
    

    // kafka consumes the message and notify the customer 
    @KafkaListener(topics = "transaction.otp.generated")
    public void consumeOtpGenerated(
        @Payload Map<String, Object> payload
    ) {

        try {
            
            String accountNumber = (String) payload.get("accountNumber");
            String otp = (String) payload.get("otp");
            String transactionId = (String) payload.get("transactionId");
            String amount = payload.get("amount").toString();
            String reason = (String) payload.get("reason");

            sendAlert( accountNumber,
                "TRANSACTION VERIFICATION REQUIRED",
                String.format("Suspicious activity detected on your account " +
                              "Reason: %s "+
                              "A transaction of %s is pending verification."+ 
                              "Your OTP is: %s. Valid for 5 minutes "+
                              "If this was not you ignore the message.")
            );


        } catch (Exception e) {
            // TODO: handle exception
            log.error("Error sending OTP notification: {}", e.getMessage());
        }
    }

    private void sendAlert(String accountNumber,
                           String subject,
                           String message) {

                            // sending the alert to the users
    }
}
