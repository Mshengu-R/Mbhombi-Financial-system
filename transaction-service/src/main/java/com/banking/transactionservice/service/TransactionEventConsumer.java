package com.banking.transactionservice.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.banking.transactionservice.entity.Transaction;
import com.banking.transactionservice.entity.TransactionStatus;
import com.banking.transactionservice.repository.TransactionRepository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionEventConsumer {
    
    private final RedisTemplate<String, Object> redisTemplate;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final TransactionService transactionService;

    private final TransactionRepository transactionRepository;

    private static final long OTP_EXPIRY_MINUTES = 5;

    private static final String TRANSACTION_OTP_GENERATED_TOPIC = "transaction.otp.generated";
    

    // public TransactionEventConsumer(TransactionRepository transactionRepository,
    //                                KafkaTemplate<String, Object> kafkaTemplate, 
    //                                 RedisTemplate<String, Object> redisTemplate) {
    //     this.kafkaTemplate = kafkaTemplate;
    //     this.redisTemplate = redisTemplate;
    //     this.transactionRepository = transactionRepository;
    // }

    // if fraud is detected
    @SuppressWarnings("deprecation")
    @KafkaListener(topics = "verification.required")
    public void consumeVerificationRequired(
        @Payload Map<String, Object> payload
    ) {
        try {
            
            String transactionId = (String) payload.get("transactionId");
            String accountNumber = (String) payload.get("accountNumber");
            String reason = (String) payload.get("reason");


            log.info("Verification required - transaction: {} reason: {}", transactionId, reason);

            Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(()-> new RuntimeException("Transaction not found ".concat(transactionId)));

            // item pontency in oder to prevent two time credit to my reciever or sender
            if(transaction.getStatus() !=  TransactionStatus.PROCCESSING) {
                log.warn("Transaction {} not Processing - skiping", transactionId);
                return;
            }

            // six digit otp
            String otp = String.format("%06d", (int) (Math.random() * 900000) + 100000);

            // stores in redis
            String otpKey = "verification:otp" + transactionId;

            // set warning for depricitiatinng has been ignored
            redisTemplate.opsForValue().set( otpKey,
                                             otp,
                                             OTP_EXPIRY_MINUTES,
                                             TimeUnit.MINUTES );

            // Update status
            transaction.setStatus(TransactionStatus.PENDING_VERIFICATION);
            transactionRepository.save(transaction);

            log.info("OTP generated for transaction: {} expires in {} min",
                              transactionId, OTP_EXPIRY_MINUTES);

            // Notify the customer
            Map<String, Object> otpEvent = new HashMap<>();
            otpEvent.put("transactionId", transactionId);
            otpEvent.put("accountNumber", accountNumber);
            otpEvent.put("reason", reason);
            otpEvent.put("otp", otp);
            otpEvent.put("amount", payload.get("amount"));

            kafkaTemplate.send(TRANSACTION_OTP_GENERATED_TOPIC, transactionId, otpEvent);



        } catch (Exception e) {
            // TODO: handle exception
        log.info("Error handling verification required", e.getMessage());
        }
    }

    @KafkaListener(topics = "fraud.check.clean")
    public void consumerFraudCheckCleanResult(
        @Payload Map<String, Object> payload) {

            try {
                

                String transactionId = (String) payload.get("transactionId");
                transactionService.processCleanResult(transactionId);

            } catch (Exception e) {
                // TODO: handle exception
                log.error("Error processing fraud chck result: {}", e.getMessage());
            }
    }
}
