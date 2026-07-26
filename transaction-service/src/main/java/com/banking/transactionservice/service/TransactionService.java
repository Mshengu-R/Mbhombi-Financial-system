package com.banking.transactionservice.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.transactionservice.client.AccountServiceClient;
import com.banking.transactionservice.dto.TransactionRequest;
import com.banking.transactionservice.dto.TransactionResponse;
import com.banking.transactionservice.entity.Transaction;
import com.banking.transactionservice.entity.TransactionStatus;
import com.banking.transactionservice.entity.TransactionType;
import com.banking.transactionservice.event.TransactionInitiatedEvent;
import com.banking.transactionservice.repository.TransactionRepository;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor


public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TRANSACTION_INITIATED_TOPIC = "transaction.initiated";
    private static final String TRANSACTION_COMPLETED_TOPIC = "transaction.completed";
    private static final String TRANSACTION_REFUNDED_TOPIC = "transaction.refunded";



    // SAGA STEP 1
    // This method initiates a transfer transaction by deducting the amount from the sender's account, saving the transaction as processing, and publishing a TransactionInitiatedEvent to Kafka for further processing.
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
            request.getAmount());

    

    // SAVE TRANSACTION AS PROCCESSING
        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber(request.getSenderAccountNumber());
        transaction.setRecieverAccountNumber(request.getReceiverAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PROCCESSING);
        transaction.setDescription(request.getDescription());
        transaction.setReferenceNumber(UUID.randomUUID().toString()); // generated reference number

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

        kafkaTemplate.send(TRANSACTION_INITIATED_TOPIC, savedTransaction.getId(), event);
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

    return transactionRepository.findBySenderAccountNumberOderedByCreatedAtDesc(accountNumber)
    .stream()
    .map(this::mapToResponse)
    .collect(Collectors.toList());


} // query by the database


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




    

}
