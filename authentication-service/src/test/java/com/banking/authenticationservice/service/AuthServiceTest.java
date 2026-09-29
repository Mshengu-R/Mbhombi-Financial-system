package com.banking.authenticationservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.banking.authenticationservice.entity.Customer;
import com.banking.authenticationservice.entity.CustomerRole;
import com.banking.authenticationservice.entity.CustomerStatus;
import com.banking.authenticationservice.repository.ConsentRepository;
import com.banking.authenticationservice.repository.UserCredentialsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserCredentialsRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @Mock
    private ConsentRepository consentRepository;

    @Test
    void saveUserDefaultsUsernameStatusAndRoleBeforePersistence() {
        AuthService authService = new AuthService(
                repository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                consentRepository
        );
        Customer customer = new Customer();
        customer.setEmail("customer@example.com");
        customer.setPhone("5551234567");
        customer.setPassword("plain-password");

        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

        authService.saveUser(customer);

        assertEquals("customer@example.com", customer.getUsername());
        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
        assertEquals(CustomerRole.CUSTOMER, customer.getRole());
        verify(repository).save(customer);
    }
}