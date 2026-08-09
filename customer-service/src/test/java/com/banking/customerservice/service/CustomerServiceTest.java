package com.banking.customerservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banking.customerservice.dto.CustomerCreateRequest;
import com.banking.customerservice.dto.CustomerResponse;
import com.banking.customerservice.entity.Customer;
import com.banking.customerservice.entity.CustomerCountry;
import com.banking.customerservice.entity.CustomerEthnicity;
import com.banking.customerservice.entity.CustomerGender;
import com.banking.customerservice.entity.CustomerLanguage;
import com.banking.customerservice.entity.CustomerRole;
import com.banking.customerservice.entity.CustomerTitle;
import com.banking.customerservice.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private NotificationService notificationService;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, notificationService);
    }

    @Test
    void createAccountShouldPersistProfileAndReturnResponse() {
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setEmail("user@example.com");
        request.setPhone("27761234567");
        request.setIdno(new BigDecimal("9801011234567"));
        request.setTitle("MR");
        request.setName("Lerato");
        request.setSurname("Mokoena");
        request.setDateOfBirth(new Date(0));
        request.setCustomerLanguage("ENGLISH");
        request.setGender("MALE");
        request.setEthnicity("AFRICAN");
        request.setCountry("SOUTH_AFRICA");

        Customer savedCustomer = new Customer();
        savedCustomer.setId("customer-1");
        savedCustomer.setEmail(request.getEmail());
        savedCustomer.setPhone(request.getPhone());
        savedCustomer.setIdno(request.getIdno());
        savedCustomer.setTitle(CustomerTitle.MR);
        savedCustomer.setName(request.getName());
        savedCustomer.setSurname(request.getSurname());
        savedCustomer.setDateOfBirth(request.getDateOfBirth());
        savedCustomer.setRole(CustomerRole.CUSTOMER);
        savedCustomer.setCustomerLanguage(CustomerLanguage.ENGLISH);
        savedCustomer.setGender(CustomerGender.MALE);
        savedCustomer.setEthnicity(CustomerEthnicity.AFRICAN);
        savedCustomer.setCountry(CustomerCountry.SOUTH_AFRICA);

        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(customerRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        CustomerResponse response = customerService.createAccount(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getRole()).isEqualTo(CustomerRole.CUSTOMER.name());
        assertThat(response.getTitle()).isEqualTo(CustomerTitle.MR);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo(request.getEmail());
        assertThat(captor.getValue().getRole()).isEqualTo(CustomerRole.CUSTOMER);
    }
}
