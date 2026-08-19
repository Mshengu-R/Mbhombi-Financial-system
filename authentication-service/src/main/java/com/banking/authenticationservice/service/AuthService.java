package com.banking.authenticationservice.service;

import java.util.Objects;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banking.authenticationservice.dto.AuthorizationRequest;
import com.banking.authenticationservice.entity.Customer;
import com.banking.authenticationservice.entity.CustomerCountry;
import com.banking.authenticationservice.entity.CustomerEthnicity;
import com.banking.authenticationservice.entity.CustomerGender;
import com.banking.authenticationservice.entity.CustomerLanguage;
import com.banking.authenticationservice.entity.CustomerRole;
import com.banking.authenticationservice.entity.CustomerTitle;
import com.banking.authenticationservice.repository.UserCredentialsRepository;

@Service
public class AuthService {

    private final UserCredentialsRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public AuthService(
            UserCredentialsRepository repository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JWTService jwtService
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Registers and persists a new customer.
     *
     * Passwords must be encoded before the entity is saved.
     */
@Transactional
public String saveUser(Customer customer) {
    Objects.requireNonNull(customer, "Customer must not be null");

    if (customer.getUsername() == null
            || customer.getUsername().isBlank()) {
        throw new IllegalArgumentException(
                "Username must not be empty"
        );
    }

    if (customer.getEmail() == null
            || customer.getEmail().isBlank()) {
        throw new IllegalArgumentException(
                "Email must not be empty"
        );
    }

    if (customer.getPhone() == null
            || customer.getPhone().isBlank()) {
        throw new IllegalArgumentException(
                "Phone number must not be empty"
        );
    }

    if (customer.getPassword() == null
            || customer.getPassword().isBlank()) {
        throw new IllegalArgumentException(
                "Password must not be empty"
        );
    }

    if (repository.existsByUsername(customer.getUsername())) {
        throw new IllegalStateException(
                "Username is already registered"
        );
    }

    if (repository.existsByEmail(customer.getEmail())) {
        throw new IllegalStateException(
                "Email address is already registered"
        );
    }

    if (repository.existsByPhone(customer.getPhone())) {
        throw new IllegalStateException(
                "Phone number is already registered"
        );
    }

    customer.setPassword(
            passwordEncoder.encode(customer.getPassword())
    );

    repository.save(customer);

    return "Customer credentials have been saved successfully";
}

    /**
     * Authenticates the supplied username and password and returns a JWT.
     */
    public String authenticate(Customer customer) {
        Objects.requireNonNull(customer, "Customer credentials must not be null");

        if (customer.getUsername() == null ||
                customer.getUsername().isBlank() ||
                customer.getPassword() == null ||
                customer.getPassword().isBlank()) {

            throw new BadCredentialsException(
                    "Username and password are required"
            );
        }

        Authentication authentication =
                authenticationManager.authenticate(
                        UsernamePasswordAuthenticationToken.unauthenticated(
                                customer.getUsername(),
                                customer.getPassword()
                        )
                );

        if (!authentication.isAuthenticated()) {
            throw new BadCredentialsException(
                    "Invalid username or password"
            );
        }

        /*
         * Generate the token using the authenticated principal rather than
         * blindly trusting the username supplied in the request.
         */
        return jwtService.generateToken(authentication.getName());
    }

    /**
     * Temporary compatibility method for the current AuthController.
     *
     * Your controller currently calls validateToken(Customer), although this
     * method authenticates credentials rather than validating an existing JWT.
     * Change the controller to call authenticate(Customer), then remove this
     * method.
     */
    @Deprecated(forRemoval = true)
    public String validateToken(Customer customer) {
        return authenticate(customer);
    }

    /**
     * Creates a Customer entity from a registration request and the resolved
     * ER-model values.
     */
    public Customer buildCustomer(
            AuthorizationRequest request,
            CustomerRole role,
            CustomerCountry country,
            CustomerTitle title,
            CustomerLanguage language,
            CustomerGender gender,
            CustomerEthnicity ethnicity
    ) {
        Objects.requireNonNull(
                request,
                "Authorization request must not be null"
        );

        Customer customer = new Customer();

        customer.setEmail(request.getEmail());
        customer.setName(request.getName());
        customer.setSurname(request.getSurname());
        customer.setPhone(request.getPhone());

        customer.setRole(role);
        customer.setCountry(country);
        customer.setTitle(title);
        customer.setCustomerLanguage(language);
        customer.setGender(gender);
        customer.setEthnicity(ethnicity);

        return customer;
    }
}