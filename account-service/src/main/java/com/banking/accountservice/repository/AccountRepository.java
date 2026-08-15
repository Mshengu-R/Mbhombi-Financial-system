package com.banking.accountservice.repository;

import com.banking.accountservice.entity.Account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// because of our id type is string
public interface AccountRepository extends JpaRepository<Account, String>{
    
    // boolean existsByEmail(String email);
    boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findByAccountNumber(String accountNumber);
    // boolean exists
}
