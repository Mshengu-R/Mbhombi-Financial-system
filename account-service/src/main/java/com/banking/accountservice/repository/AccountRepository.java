package com.banking.accountservice.repository;

import com.banking.accountservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

// because of our id type is string
public interface AccountRepository extends JpaRepository<Account, String>{
    
}
