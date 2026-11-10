// package com.banking.authenticationservice.service;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.util.UUID;

// import com.banking.authenticationservice.dto.AuthLoginRequest;
// import com.banking.authenticationservice.dto.AuthRegistrationRequest;
// import com.banking.authenticationservice.entity.Customer;
// import com.banking.authenticationservice.entity.CustomerRole;
// import com.banking.authenticationservice.security.CustomerPrincipal;
// import com.banking.authenticationservice.repository.ConsentRepository;
// import com.banking.authenticationservice.repository.UserCredentialsRepository;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.crypto.password.PasswordEncoder;

// @ExtendWith(MockitoExtension.class)
// class AuthServiceTest {

//     @Mock
//     private UserCredentialsRepository repository;

//     @Mock
//     private PasswordEncoder passwordEncoder;

//     @Mock
//     private AuthenticationManager authenticationManager;

//     @Mock
//     private JWTService jwtService;

//     @Mock
//     private ConsentRepository consentRepository;

//     @Test
//     void saveUserPersistsOnlyRequestedFieldsAndAssignsCustomerRole() {
//         AuthService authService = new AuthService(
//                 repository,
//                 passwordEncoder,
//                 authenticationManager,
//                 jwtService,
//                 consentRepository
//         );
//         AuthRegistrationRequest request = new AuthRegistrationRequest();
//         request.setName("Customer Name");
//         request.setEmail("customer@example.com");
//         request.setUsername("customer");
//         request.setPassword("plain-password");

//         when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

//         authService.saveUser(request);

//         org.mockito.ArgumentCaptor<Customer> savedCustomer =
//                 org.mockito.ArgumentCaptor.forClass(Customer.class);
//         verify(repository).save(savedCustomer.capture());
//         assertEquals("Customer Name", savedCustomer.getValue().getName());
//         assertEquals("customer@example.com", savedCustomer.getValue().getEmail());
//         assertEquals("customer", savedCustomer.getValue().getUsername());
//         assertEquals("encoded-password", savedCustomer.getValue().getPassword());
//         assertEquals(CustomerRole.CUSTOMER, savedCustomer.getValue().getRole());
//     }

//     @Test
//     void authenticateUsesAuthenticatedPrincipalToIssueToken() {
//         AuthService authService = new AuthService(
//                 repository,
//                 passwordEncoder,
//                 authenticationManager,
//                 jwtService,
//                 consentRepository
//         );
//         Customer customer = new Customer();
//         customer.setId(UUID.randomUUID());
//         customer.setUsername("customer");
//         customer.setEmail("customer@example.com");
//         customer.setPassword("encoded-password");
//         customer.setRole(CustomerRole.CUSTOMER);
//         CustomerPrincipal principal = CustomerPrincipal.from(customer);
//         Authentication authenticated = UsernamePasswordAuthenticationToken.authenticated(
//                 principal,
//                 null,
//                 principal.getAuthorities()
//         );
//         AuthLoginRequest request = new AuthLoginRequest();
//         request.setUsername("customer");
//         request.setPassword("plain-password");

//         when(authenticationManager.authenticate(any(Authentication.class)))
//                 .thenReturn(authenticated);
//         when(jwtService.generateToken("customer", customer.getId().toString(), "CUSTOMER"))
//                 .thenReturn("signed-token");

//         assertEquals("signed-token", authService.authenticate(request));
//         verify(authenticationManager).authenticate(any(Authentication.class));
//         verify(jwtService).generateToken(
//                 "customer",
//                 customer.getId().toString(),
//                 "CUSTOMER"
//         );
//     }
// }