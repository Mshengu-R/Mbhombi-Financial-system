package com.banking.authenticationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banking.authenticationservice.entity.Customer;

public interface UserCredentialsRepository extends JpaRepository<Customer, String>{
    
    Customer findByUsername(String username);

}
