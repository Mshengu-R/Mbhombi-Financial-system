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
                              "If this was not you ignore the message.",
                               reason, transactionId, amount, otp)
            );


        } catch (Exception e) {
            
            log.error("Error sending OTP notification: {}", e.getMessage());
        }
    }

// @KafkaListener(topics = "transaction.completed")
public void consumeTransactionCompleted(@Payload Map<String, Object> payload) {

    try {

        String senderAccount = (String) payload.get("senderAccountNumber");
        String recieverAccount = (String) payload.get("recieverAccountNumber");
        String amount = payload.get("amount").toString();

        sendAlert(senderAccount, "DEBIT ALERT",
                    String.format( "%s debited from account %s",
                            amount, recieverAccount));
        
    } catch (Exception e) {
    log.error("Error sending transaction notification", e.getMessage());
    }

}



@KafkaListener(topics = "fraud.detected")
public void consumeFraudDetected(@Payload Map<String, Object> payload) {

    try{
        String accountNumber = (String) payload.get("accountNumber");
        String reason = (String) payload.get("reason");

        sendAlert(accountNumber, "FRAUD ALERT",
                    String.format("Suspicious activity detected on your account. "+
                                  "Reason: %s. Your account has been blocked for security reasons. "+ 
                                  "Please contact customer support for assistance. ",
                                  reason));
    }catch(Exception e){
        log.error("Error sending fraud notification: {}", e.getMessage());
    }
}


// kafka consumes the message and notify the customer
@KafkaListener(topics = "transaction.refunded")
public void consumeTransactionRefunded(@Payload Map<String, Object> payload) {

    try {
        String senderAccount = (String) payload.get("senderAccountNumber");
        String amount = payload.get("amount").toString();
        String reason = (String) payload.get("reason");

        sendAlert(senderAccount, "REFUND ALERT",
                    String.format("Reason: %s "+ "A refund of %s has been processed for your account %s",
                            reason, amount, senderAccount));

    } catch (Exception e) {
        log.error("Error sending refund notification: {}", e.getMessage());
    }

}

@KafkaListener(topics = "payment.completed")
public void consumePaymentCompleted(@Payload Map<String, Object> payload) {
    
    try {
        String accountNumber = (String) payload.get("senderAccountNumber");
        String amount = payload.get("amount").toString();
        String reason = (String) payload.get("reason");

        sendAlert(accountNumber, "PAYMENT SUCCESSFUL",
                    String.format("Reason: %s "+ "A payment of %s has been processed for your account %s",
                            reason, amount, accountNumber));

    } catch (Exception e) {
        log.error("Error sending payment notification: {}", e.getMessage());
    }
}

@KafkaListener(topics = "payment.failed")
public void consumePaymentFailed(@Payload Map<String, Object> payload) {
    try {
        String accountNumber = (String) payload.get("senderAccountNumber");
        String amount = payload.get("amount").toString();
        String reason = (String) payload.get("reason");

        sendAlert(accountNumber, "PAYMENT FAILED",
                    String.format("Reason: %s "+ "A payment of %s could not be processed for your account %s",
                            reason, amount, accountNumber));

    } catch (Exception e) {
        log.error("Error sending payment failed notification: {}", e.getMessage());
    }
}

    private void sendAlert(String accountNumber,
                           String subject,
                           String message) {

                            log.info("-------------------");
                            log.info("Account: {}", accountNumber);
                            log.info("Subject: {}", subject);
                            log.info("Message: {}", message);
                            log.info("-------------------");

                            // sending the alert to the users
    }
}
