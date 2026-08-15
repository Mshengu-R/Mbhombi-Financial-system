package com.banking.transactionservice.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.transactionservice.client.AccountServiceClient;
import com.banking.transactionservice.dto.TransactionRequest;
import com.banking.transactionservice.dto.TransactionResponse;
import com.banking.transactionservice.entity.Transaction;
import com.banking.transactionservice.entity.TransactionStatus;
import com.banking.transactionservice.entity.TransactionType;
import com.banking.transactionservice.event.TransactionCompletedEvent;
import com.banking.transactionservice.event.TransactionInitiatedEvent;
import com.banking.transactionservice.repository.TransactionRepository;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for orchestrating transactions (SAGA initiator responsibilities).
 *
 * Responsibilities:
 * - Interact with the AccountService (via Feign) to debit/credit accounts
 * - Persist Transaction entities
 * - Publish domain events to Kafka for downstream processing (fraud detection, completion, refunds)
 * - Use Redis for transient state (OTP storage)
 *
 * The implementation intentionally keeps SAGA actions idempotent where practical and emits events
 * for the rest of the process so other services can react asynchronously.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TRANSACTION_INITIATED_TOPIC = "transaction.initiated";
    private static final String TRANSACTION_COMPLETED_TOPIC = "transaction.completed";
    private static final String TRANSACTION_REFUNDED_TOPIC = "transaction.refunded";
    private static final String FRAUD_DETECTED_TOPIC = "fraud.detected";


    // SAGA STEP 1
    // This method initiates a transfer transaction by deducting the amount from the sender's account, saving the transaction as processing, and publishing a TransactionInitiatedEvent to Kafka for further processing.
    /**
     * Initiates a transfer SAGA.
     *
     * Steps (high level):
     * 1. Request the Account service to deduct the sender's balance
     * 2. Persist a Transaction with status PROCCESSING
     * 3. Publish a TransactionInitiatedEvent to Kafka so downstream services (fraud check, notification, credit)
     *    can continue the SAGA.
     *
     * The method returns the persisted Transaction as a TransactionResponse. Errors from downstream services
     * (e.g., account debit failures) should be handled by the caller or by adding try/catch and compensation logic.
     *
     * @param request transfer details (sender, receiver, amount, description)
     * @return persisted transaction as response DTO
     */
    public TransactionResponse transfer(TransactionRequest request){


        // LOG INFORMATION ABOUT THE SAGA PROCESS
        log.info("SAGA START - Transfer: {} -> {} amount: {}", 
            request.getSenderAccountNumber(),
            request.getReceiverAccountNumber(),
            request.getAmount()
        );

        // DEDUCT FROM SENDER
        accountServiceClient.deductBalance(
            request.getSenderAccountNumber(),
            request.getAmount()
        );

    

    // SAVE TRANSACTION AS PROCCESSING
        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber(request.getSenderAccountNumber());
        transaction.setRecieverAccountNumber(request.getReceiverAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PROCCESSING);
        transaction.setDescription(request.getDescription());
        transaction.setReferenceNumber(UUID.randomUUID().toString()); // generated reference number
        transaction.setCreatedAt(LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Transaction saved as PROCCESSING: {}", savedTransaction.getId());

        // Publish the initiated event to kafka for fraud detection SAGA STEP 2
        TransactionInitiatedEvent event = new TransactionInitiatedEvent(
            savedTransaction.getId(),
            savedTransaction.getSenderAccountNumber(),
            savedTransaction.getRecieverAccountNumber(),
            savedTransaction.getAmount(),
            savedTransaction.getDescription()
        );

        kafkaTemplate.send(  TRANSACTION_INITIATED_TOPIC, // topic name
                             savedTransaction.getId(), // key for partitioning
                             event  ); // event payload

        log.info("SAGA STEP 2 - TransactionInitiatedEvent published: {}", savedTransaction.getId());

        return mapToResponse(savedTransaction);
}


// GET TRANSACTION BY ID
public TransactionResponse getTransaction(String transactionId){
    return mapToResponse(transactionRepository

      // this is for fetching the transaction by its ID from the database. If the transaction is not found, it throws a RuntimeException with a message indicating that the transaction was not found.
        .findById(transactionId)
        .orElseThrow( // for transaction not found
            () -> new RuntimeException(
            "Transaction not found: "+transactionId
        ))
    );
}


// this method retrieves the transaction history for a specific account by fetching all transactions where the account is either the sender or receiver.
//  It maps each transaction to a TransactionResponse and returns a list of these responses.
// find by sender account number history
public List<TransactionResponse> getTransactionHistory(String accountNumber){

        // Fetch from repository using Spring Data generated query method (ordered newest-first).
        return transactionRepository.findBySenderAccountNumberOrderByCreatedAtDesc(accountNumber)
    .stream()
    .map(this::mapToResponse)
    .collect(Collectors.toList());


} // query by the database


public TransactionResponse verifyOTP(String transactionID, String otp) {
    log.info("OTP verification for the transaction with suspicious activities: {}", transactionID);

    Transaction transaction = transactionRepository.findById(transactionID)
        .orElseThrow(()-> new RuntimeException("Transaction with id \"" + transactionID +  "\" is not found"));

        String otpKey =  "verification:otp" + transactionID;
        String storedOtp = (String) redisTemplate.opsForValue().get(otpKey);

        // expired one time passoword
        if(storedOtp == null) {

            log.warn("OTP expired for the transaction: {}", transactionID);
            compesateTransaction(transaction, "OTP expired - transaction cancelled");
            return mapToResponse(transaction);
        }

        // wrong otp
        if(!storedOtp.equals(otp)) {
            // Block account but refund the customer
            log.warn("Warn OTP - blocking account and refunding: {}", transactionID);
            redisTemplate.delete(otpKey);
            blockAccountAndcompesate(transaction,
                                     "Wrong OPT entered- transaction cancelled, "+
                                     "account blocked for security purposes. "+
                                     "If wrong please contact the bank as soon as possible"
            );
            return mapToResponse(transaction);

        }

            log.info("OTP verified - completing transaction: {}", transactionID);
            redisTemplate.delete(otpKey);
            completeTransaction(transaction);
            return mapToResponse(transaction);

    }


    private void completeTransaction(Transaction transaction) {

    transaction.setStatus(TransactionStatus.COMPLETED);
    transaction.setCompletedAt(LocalDateTime.now());

    // create the object for the class
    TransactionCompletedEvent completedEvent = new  TransactionCompletedEvent(
    transaction.getId(),
    transaction.getSenderAccountNumber(),
    transaction.getRecieverAccountNumber(),
    transaction.getAmount(),
    transaction.getDescription());

kafkaTemplate.send(TRANSACTION_COMPLETED_TOPIC, transaction.getId(), completedEvent);

log.info("SAGA COMPLETED - Transaction {} completed", transaction.getId());

    // throw new UnsupportedOperationException("Unimplemented method 'completeTransaction'");
}


    /*
    * This method handles the scenario where a transaction is flagged for fraud.
    * 
    * It publishes a fraud detection event to Kafka,
    *  indicating that the account should be blocked,
    *  and then calls the compensation method to refund the sender's account.
    * 
    * The method logs relevant information about the fraud detection and compensation process.
    */ 
    private void blockAccountAndcompesate(Transaction transaction, String reason) {
    // publish fraud detection event
    Map<String, Object> fraudEvent = new HashMap<>();
    fraudEvent.put("transactionId", transaction.getId());
    fraudEvent.put("accountNumber", transaction.getRecieverAccountNumber());
    fraudEvent.put("reason", reason);

    kafkaTemplate.send(FRAUD_DETECTED_TOPIC, transaction.getSenderAccountNumber(), fraudEvent);

    log.warn("Fraud detected and published - account: {} will be blocked, Kindly contact the bank or visit our nearest branch for further enquiries",
        transaction.getSenderAccountNumber()
    );

    // compesate the customer
    compesateTransaction(transaction, reason);


    // throw new UnsupportedOperationException("Unimplemented method 'blockAccountAndcompesate'");
}


    // compesate or credit back the sender
    private void compesateTransaction(Transaction transaction, String reason) {
        log.warn("SAGA COMPESATION - refunding: {} amount: {}",
           transaction.getSenderAccountNumber(),
           transaction.getAmount()

        );

        // call the account client, Notification service will alert the user
        accountServiceClient.creditBalance(transaction.getSenderAccountNumber(),
                                           transaction.getAmount());
        
                                           transaction.setStatus(TransactionStatus.FLAGGED);
                                           transaction.setFailureReason(reason +
                                                " - SAGA Compesation executed, amount refunded at "+ LocalDateTime.now());
                                           transactionRepository.save(transaction);
        // notification
        Map<String, Object> refundEvent = new HashMap<>();
        refundEvent.put("transactionId", transaction.getId());
        refundEvent.put("senderAccountnumber", transaction.getSenderAccountNumber());
        refundEvent.put("amount", transaction.getAmount());
        refundEvent.put("reason", reason);

        kafkaTemplate.send(TRANSACTION_REFUNDED_TOPIC, transaction.getId(), refundEvent);

        log.info("SAGA compesation complete - {} refunded to {}",
         transaction.getAmount(),
        transaction.getSenderAccountNumber());
    }
// 
private TransactionResponse mapToResponse(Transaction transaction) {
    
    TransactionResponse response = new TransactionResponse();

    response.setId(transaction.getId());
    response.setSenderAccountNumber(transaction.getSenderAccountNumber());
    response.setRecieverAccountNumber(transaction.getRecieverAccountNumber());
    response.setAmount(transaction.getAmount());
    response.setType(transaction.getType());
    response.setStatus(transaction.getStatus());
    response.setDescription(transaction.getDescription());
    response.setFailureReason(transaction.getFailureReason());
    response.setReferenceNumber(transaction.getReferenceNumber());
    response.setCreatedAt(transaction.getCreatedAt());
    response.setCompletedAt(transaction.getCompletedAt());
    return response;

}


public void processCleanResult(String transactionId) {

    Transaction transaction = transactionRepository.findById(transactionId)
    .orElseThrow(()-> new RuntimeException(
        "Transaction not found "+transactionId
    ));

        if(transaction.getStatus() !=  TransactionStatus.PROCCESSING) {
                log.warn("Transaction {} not Processing - skiping ", transactionId);
                return;
            }

            completeTransaction(transaction);

    // throw new UnsupportedOperationException("Unimplemented method 'processCleanResult'");
}




    

}