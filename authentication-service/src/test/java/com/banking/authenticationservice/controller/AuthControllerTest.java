package com.banking.authenticationservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.banking.authenticationservice.config.SecurityConfig;
import com.banking.authenticationservice.entity.Customer;
import com.banking.authenticationservice.repository.UserCredentialsRepository;
import com.banking.authenticationservice.service.AuthService;
import com.banking.authenticationservice.service.CustomerDetailsService;
import com.banking.authenticationservice.service.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserCredentialsRepository customerRepository;

    @MockitoBean
    private CustomerDetailsService customerDetailsService;

    @MockitoBean
    private JWTService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerAcceptsValidCustomerJson() throws Exception {
        when(authService.saveUser(any(Customer.class)))
                .thenReturn("Customer credentials have been saved successfully");

        mockMvc.perform(post("/api/v1/auth/register")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer expired-or-malformed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCustomerJson()))
                .andExpect(status().isCreated());

        verify(authService).saveUser(any(Customer.class));
    }

    @Test
    void registerRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idNumber\":\"0705267470193\" \"phone\":\"0152971855\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerReturnsConflictForDuplicateCustomer() throws Exception {
        when(authService.saveUser(any(Customer.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Customer already exists"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCustomerJson()))
                .andExpect(status().isConflict());
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() throws Exception {
        when(authService.authenticate(any(Customer.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"customer@example.com","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturnsTokenForValidCredentials() throws Exception {
        when(authService.authenticate(any(Customer.class))).thenReturn("signed-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"customer@example.com","password":"correct"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("signed-token"));

        verify(authService).authenticate(any(Customer.class));
    }

    @Test
    void protectedEndpointReturnsUnauthorizedWithoutJwt() throws Exception {
        mockMvc.perform(post("/api/v1/auth/consents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointReturnsUnauthorizedForInvalidJwt() throws Exception {
        mockMvc.perform(post("/api/v1/auth/consents")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer expired-or-malformed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsPreflightForRegistrationIsAccepted() throws Exception {
        mockMvc.perform(options("/api/v1/auth/register")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
    }

    private String validCustomerJson() {
        return """
                {
                  "email": "JabuDoe@gmail.com",
                  "username": "JabuDoe",
                  "password": "#JabuDoe123",
                  "firstName": "Jabulani",
                  "secondName": "",
                  "name": "Jabulani",
                  "surname": "Doe",
                  "idNumber": "0705267470193",
                  "phone": "0152971855",
                  "role": "CUSTOMER",
                  "country": "SOUTH_AFRICA",
                  "title": "MR",
                  "customerLanguage": "XITSONGA",
                  "gender": "MALE",
                  "ethnicity": "AFRICAN",
                  "dateOfBirth": "2007-05-26",
                  "homeAddress": [],
                  "otherAddress": []
                }
                """;
    }
}
