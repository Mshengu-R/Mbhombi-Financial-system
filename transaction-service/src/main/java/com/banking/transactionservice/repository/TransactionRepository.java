package com.banking.transactionservice.repository;

import com.banking.transactionservice.entity.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String>{

    /**
     * Finds transactions for a given sender account ordered by creation time (newest first).
     *
     * Note: Spring Data method name must follow the 'OrderBy' pattern so the query is generated
     * automatically. The previous (misspelled) method name would fail at startup.
     *
     * @param accountNumber sender account number to filter by
     * @return list of transactions ordered by createdAt descending
     */
    List<Transaction> findBySenderAccountNumberOrderByCreatedAtDesc(String accountNumber);

    
}
