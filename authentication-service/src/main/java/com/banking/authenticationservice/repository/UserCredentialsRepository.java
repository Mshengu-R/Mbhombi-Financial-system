package com.banking.authenticationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banking.authenticationservice.entity.UserCredentials;

public interface UserCredentialsRepository extends JpaRepository<UserCredentials, String>{
    
    
}
