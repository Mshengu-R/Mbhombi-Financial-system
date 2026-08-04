package com.banking.communityaccountservice.repository;

import com.banking.communityaccountservice.entity.CommunityAccount;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// because of our id type is string
public interface CommunityAccountRepository extends JpaRepository<CommunityAccount, String>{
    
    // boolean existsByEmail(String email);
    boolean existsByAccountNumber(String accountNumber);
    Optional<CommunityAccount> findByAccountNumber(String accountNumber);
}
