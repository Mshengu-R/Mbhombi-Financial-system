package com.banking.authenticationservice.service;

import java.util.Objects;
import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banking.authenticationservice.dto.AuthorizationRequest;
import com.banking.authenticationservice.dto.ConsentRequest;
import com.banking.authenticationservice.entity.Consent;
import com.banking.authenticationservice.entity.Customer;
import com.banking.authenticationservice.entity.CustomerCountry;
import com.banking.authenticationservice.entity.CustomerEthnicity;
import com.banking.authenticationservice.entity.CustomerGender;
import com.banking.authenticationservice.entity.CustomerLanguage;
import com.banking.authenticationservice.entity.CustomerRole;
import com.banking.authenticationservice.entity.CustomerStatus;
import com.banking.authenticationservice.entity.CustomerTitle;
import com.banking.authenticationservice.repository.UserCredentialsRepository;
import com.banking.authenticationservice.repository.ConsentRepository;
import com.banking.authenticationservice.security.CustomerPrincipal;

@Service
public class AuthService {

        private static final Logger log = LoggerFactory.getLogger(AuthService.class);
        private static final CustomerStatus DEFAULT_CUSTOMER_STATUS = CustomerStatus.ACTIVE;
        private static final CustomerRole DEFAULT_CUSTOMER_ROLE = CustomerRole.CUSTOMER;

    private final UserCredentialsRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
        private final ConsentRepository consentRepository;

    public AuthService(
            UserCredentialsRepository repository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JWTService jwtService,
            ConsentRepository consentRepository
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.consentRepository = consentRepository;
    }

    /**
     * Registers and persists a new customer.
     *
     * Passwords must be encoded before the entity is saved.
     */
@Transactional
public String saveUser(Customer customer) {
    Objects.requireNonNull(customer, "Customer must not be null");

//     if (customer.getUsername() == null
//             || customer.getUsername().isBlank()) {
//         throw new IllegalArgumentException(
//                 "Username must not be empty"
//         );
//     }

    if (customer.getEmail() == null || customer.getEmail().isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Email must not be empty"
        );
    }

        customer.setUsername(customer.getEmail());
        customer.setStatus(DEFAULT_CUSTOMER_STATUS);
        customer.setRole(DEFAULT_CUSTOMER_ROLE);

    if (customer.getPhone() == null
            || customer.getPhone().isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Phone number must not be empty"
        );
    }

    if (customer.getPassword() == null
            || customer.getPassword().isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Password must not be empty"
        );
    }

    if (repository.existsByUsername(customer.getUsername())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Username is already registered"
        );
    }

    if (repository.existsByEmail(customer.getEmail())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Email address is already registered"
        );
    }

    if (repository.existsByPhone(customer.getPhone())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Phone number is already registered"
        );
    }

    customer.setPassword(
            passwordEncoder.encode(customer.getPassword())
    );

    repository.save(customer);
        log.info("Registered customer username={} customerId={}", customer.getUsername(), customer.getId());

    return "Customer credentials have been saved successfully. Your username is your email address.";
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
        CustomerPrincipal principal = (CustomerPrincipal) authentication.getPrincipal();
        log.info("Authenticated customer username={} customerId={}",
                principal.getUsername(), principal.getCustomerId());
        return jwtService.generateToken(
                principal.getUsername(),
                principal.getCustomerId().toString(),
                principal.getAuthorities().iterator().next().getAuthority().substring("ROLE_".length())
        );
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
            CustomerStatus status,
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
        customer.setUsername(customer.getEmail());
        customer.setPassword(request.getPassword());
        customer.setFullNames(request.getFullNames());
        customer.setSecondName(request.getSecondName());
        // customer.setName(request.getName());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setSurname(request.getSurname());
        customer.setIdNumber(request.getIdno());
        customer.setPhone(request.getPhone());
        customer.setStatus(DEFAULT_CUSTOMER_STATUS);
        customer.setRole(DEFAULT_CUSTOMER_ROLE);
        customer.setCountry(country);
        customer.setTitle(title);
        customer.setCustomerLanguage(language);
        customer.setGender(gender);
        customer.setEthnicity(ethnicity);

        return customer;
    }

        @Transactional
        public Consent recordConsent(Customer customer, ConsentRequest request) {
                if (consentRepository.existsByCustomerIdAndConsentTypeAndDocumentVersion(
                                customer.getId(), request.getConsentType(), request.getDocumentVersion())) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "This consent version was already recorded"
                        );
                }
                Consent consent = new Consent();
                consent.setCustomer(customer);
                consent.setConsentType(request.getConsentType());
                consent.setDocumentVersion(request.getDocumentVersion().trim());
                consent.setGranted(true);
                consent.setGrantedAt(LocalDateTime.now());
                return consentRepository.save(consent);
        }
}