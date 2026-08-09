package com.banking.authenticationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.banking.authenticationservice.entity.UserCredentials;
import com.banking.authenticationservice.repository.UserCredentialsRepository;

@Service
public class AuthService {
    
    @Autowired
    private UserCredentialsRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;

    public String saveUser(UserCredentials credentials){
        credentials.setPassword(passwordEncoder.encode(credentials.getPassword()));
        repository.save(credentials);
        return "You credentials have been saved";

    }


    // method to generate token
    public String generateToken(String username) {
        return jwtService.generateToken(username);
    }

    // method to validate the token
    public void validateToken(String token) {
        jwtService.validateToken(token);
    }

}
