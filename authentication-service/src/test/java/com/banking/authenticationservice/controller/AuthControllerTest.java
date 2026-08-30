package com.banking.authenticationservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.banking.authenticationservice.entity.Customer;
import com.banking.authenticationservice.repository.UserCredentialsRepository;
import com.banking.authenticationservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserCredentialsRepository customerRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService, customerRepository))
                .build();
    }

    @Test
    void registerAcceptsValidCustomerJson() throws Exception {
        when(authService.saveUser(any(Customer.class)))
                .thenReturn("Customer credentials have been saved successfully");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(validCustomerJson()))
                .andExpect(status().isOk());

        verify(authService).saveUser(any(Customer.class));
    }

    @Test
    void registerRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("{\"idNumber\":\"0705267470193\" \"phone\":\"0152971855\"}"))
                .andExpect(status().isBadRequest());
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
