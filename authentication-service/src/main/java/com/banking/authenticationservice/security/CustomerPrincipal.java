package com.banking.authenticationservice.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.banking.authenticationservice.entity.Customer;

public final class CustomerPrincipal implements UserDetails {

    private final UUID customerId;
    private final String username;
    private final String password;
    private final String email;
    private final String role;

    private CustomerPrincipal(
            UUID customerId,
            String username,
            String password,
            String email,
            String role
    ) {
        this.customerId = customerId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public static CustomerPrincipal from(Customer customer) {
        return new CustomerPrincipal(
                customer.getId(),
                customer.getUsername(),
                customer.getPassword(),
                customer.getEmail(),
                customer.getRole() == null ? "CUSTOMER" : customer.getRole().name()
        );
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}