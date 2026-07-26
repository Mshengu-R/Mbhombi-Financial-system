package com.banking.transactionservice.repository;

import com.banking.transactionservice.entity.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String>{

    List<Transaction> findBySenderAccountNumberOderedByCreatedAtDesc(String accountNumber);

    
}
