package com.banking.frauddetectionservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.frauddetectionservice.client.AccountServiceClient;
import com.banking.frauddetectionservice.model.FraudCheckResults;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionService {

    // @Autowired
    // private StringRedisTemplate stringRedisTemplate;

    private final AccountServiceClient accountServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate; 
    
    @Value("${fraud.max-transactions-per-minute}")
    private int maxTransactionPerMinute;

    @Value("${fraud.suspicious-amount-multiplier}")
    private int suspiciousAmountMultiplier;

    @Value("${fraud.max-balance-percentage}")
    private int maxBalancePercentage;

    private static final String VERIFICATION_REQUIRED_TOPIC = "verification.required";
    private static final String FRAUD_CHECK_CLEAN_TOPIC = "fraud.check.clean";


    // public FraudDetectionService(
    //     AccountServiceClient accountServiceClient,
    //     KafkaTemplate<String, Object> kafkaTemplate,
    //     RedisTemplate<String, Object> redisTemplate
    // ) {
    //     this.accountServiceClient = accountServiceClient;
    //     this.kafkaTemplate = kafkaTemplate;
    //     this.redisTemplate = redisTemplate;
    // }

    public void checkTransaction(Map<String, Object> payload) {
        String transactionId = (String)payload.get("transactionId");
        String accountNumber = (String)payload.get("senderAccountNumber");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());


        // fetch real balance from the account service
        BigDecimal senderBalance = accountServiceClient.getBalance(accountNumber);

        log.info("Checking transaction: {} account: {} amount: {} balance: {}",
                         transactionId,
                         accountNumber,
                         amount,
                         senderBalance);

        FraudCheckResults results = performFraudDetection(accountNumber, amount, senderBalance);

        if(results.isFraud()) {

            log.info("Fraud detected - account: {} reason: {} - requesting OTP verification",
                                             accountNumber, results.getReason());

            Map<String, Object> verificationEvent = new HashMap<>();
            verificationEvent.put("transactionId", transactionId);
            verificationEvent.put("accountNumber", accountNumber);
            verificationEvent.put("amount", amount);
            verificationEvent.put("reason", results.getReason());

            kafkaTemplate.send(VERIFICATION_REQUIRED_TOPIC, transactionId, verificationEvent);
        }

        // if no fraud detected
        else {
            log.info("No fraud detected");

            Map<String, Object> cleanEvent = new HashMap<>();
            cleanEvent.put("transactionId", transactionId);
            cleanEvent.put("isFraud", false);
            cleanEvent.put("reason", null);
            // cleanEvent.put();

            kafkaTemplate.send(FRAUD_CHECK_CLEAN_TOPIC, transactionId, cleanEvent);

        }
    }

    private FraudCheckResults performFraudDetection(
        String accountNumber,
        BigDecimal amount,
        BigDecimal senderBalance
    ) {


        // velocity
        if(isVelocityExceeded(accountNumber)){
            return new FraudCheckResults(
                true, "Many transactions in the last 180 seconds"
            );
        }

        // suspicious amount
        if(isAmountSuspicious(accountNumber, amount)) {
            return new FraudCheckResults(true, "Unusual transaction amount.");
        }

        if(senderBalance.compareTo(BigDecimal.ZERO) > 0 && isBalanceCheckFailed(senderBalance, amount)) {

            return new FraudCheckResults(true, "Initiated amount exceeds 90% of account balance");
        }

        return new FraudCheckResults(false, null);
    
    }

  
    // number of transactions made in a minute or 3
    @SuppressWarnings("deprecation")
    private boolean isVelocityExceeded(String accountNumber) {
        String key = "fraud:velocity" + accountNumber;
        Long count = redisTemplate.opsForValue().increment(key);

        if(count != null && count == 3) {
            // expire has been supressed, (depriciated)
            redisTemplate.expire(key, 180, TimeUnit.SECONDS);
        }

        log.info("Velocity check - account: {} count: {}/{}", accountNumber, count, maxTransactionPerMinute);

        return count != null && count > maxTransactionPerMinute;
    }


    
    // users suspicious amount
     private boolean isAmountSuspicious(String accountNumber, BigDecimal amount) {

        String avgKey = "fraud:avg_amount:" + accountNumber;
        String avgStr = (String) redisTemplate.opsForValue().get(avgKey);

        if(avgStr == null) {

            redisTemplate.opsForValue().set(avgKey, amount.toString());
            return false;
        }
        BigDecimal avgAmount = new BigDecimal(avgStr);
        BigDecimal threshold = avgAmount.multiply(BigDecimal.valueOf(suspiciousAmountMultiplier));


        // update running av
        BigDecimal newAvg = avgAmount.add(amount)
        .divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);

        redisTemplate.opsForValue().set(avgKey, newAvg.toString());

        boolean suspicious = amount.compareTo(threshold) > 0;
        log.info("Amount check -amount: {} threshold: {} suspicious: {}",
         amount, threshold, suspicious);
        return suspicious;

     }


     // check balance of the customer and their initiated transactions
     // 90 percent balance
       private boolean isBalanceCheckFailed(BigDecimal senderBalance, BigDecimal amount) {
        BigDecimal threshold = senderBalance.multiply(BigDecimal.valueOf(maxBalancePercentage));

        log.info("Balance check - amount: {} maxAllowed: {} suspicious: {}",
            amount, threshold, amount.compareTo(threshold) > 0);

        return amount.compareTo(threshold) > 0;
    }
}
