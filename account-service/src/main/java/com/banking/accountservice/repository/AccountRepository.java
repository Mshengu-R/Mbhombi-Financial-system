package com.banking.accountservice.repository;

import com.banking.accountservice.entity.Account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// because of our id type is string
public interface AccountRepository extends JpaRepository<Account, String>{
    
    // boolean existsByAccountId(String id);
    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findAccountByCustomerId(String customerId);
    // boolean exists
}
