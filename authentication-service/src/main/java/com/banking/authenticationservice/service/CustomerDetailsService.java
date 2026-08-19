package com.banking.authenticationservice.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.banking.authenticationservice.entity.Customer;
import com.banking.authenticationservice.repository.UserCredentialsRepository;
import com.banking.authenticationservice.security.CustomerPrincipal;

@Service
public class CustomerDetailsService implements UserDetailsService {

    private final UserCredentialsRepository repository;

    public CustomerDetailsService(
            UserCredentialsRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Customer customer = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Customer was not found"
                ));

        return CustomerPrincipal.from(customer);
    }
}