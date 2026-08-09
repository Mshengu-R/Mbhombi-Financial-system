package com.banking.customerservice.service;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;

    public CustomerResponse createAccount(CustomerCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile request is required");
        }

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A profile already exists for this email address");
        }

        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A profile already exists for this phone number");
        }

        CustomerTitle customerTitle = parseCustomerTitle(request.getTitle());
        CustomerLanguage customerLanguage = parseCustomerLanguage(request.getCustomerLanguage());
        CustomerGender customerGender = parseCustomerGender(request.getGender());
        CustomerEthnicity customerEthnicity = parseCustomerEthnicity(request.getEthnicity());
        CustomerCountry customerCountry = parseCustomerCountry(request.getCountry());

        Customer customer = buildAccount(request, customerTitle, customerLanguage, customerGender, customerEthnicity, customerCountry);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer profile created successfully for email={} phone={}", savedCustomer.getEmail(), savedCustomer.getPhone());
        notificationService.sendProfileCreatedEmail(savedCustomer.getEmail(), savedCustomer.getName());

        return mapToResponse(savedCustomer);
    }

    private CustomerTitle parseCustomerTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }

        try {
            return CustomerTitle.valueOf(title.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid title: " + title, ex);
        }
    }

    private CustomerLanguage parseCustomerLanguage(String language) {
        if (language == null || language.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Language is required");
        }

        try {
            return CustomerLanguage.valueOf(language.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid language: " + language, ex);
        }
    }

    private CustomerGender parseCustomerGender(String gender) {
        if (gender == null || gender.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gender is required");
        }

        try {
            return CustomerGender.valueOf(gender.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid gender: " + gender, ex);
        }
    }

    private CustomerEthnicity parseCustomerEthnicity(String ethnicity) {
        if (ethnicity == null || ethnicity.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ethnicity is required");
        }

        try {
            return CustomerEthnicity.valueOf(ethnicity.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ethnicity: " + ethnicity, ex);
        }
    }

    private CustomerCountry parseCustomerCountry(String country) {
        if (country == null || country.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Country is required");
        }

        try {
            return CustomerCountry.valueOf(country.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid country: " + country, ex);
        }
    }

    private Customer buildAccount(CustomerCreateRequest request, CustomerTitle customerTitle,
            CustomerLanguage customerLanguage, CustomerGender customerGender,
            CustomerEthnicity customerEthnicity, CustomerCountry customerCountry) {
        Customer customer = new Customer();
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setIdno(request.getIdno());
        customer.setRole(CustomerRole.CUSTOMER);
        customer.setTitle(customerTitle);
        customer.setName(request.getName());
        customer.setSurname(request.getSurname());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setCustomerLanguage(customerLanguage);
        customer.setEthnicity(customerEthnicity);
        customer.setGender(customerGender);
        customer.setCountry(customerCountry);

        ArrayList<String> homeAddress = request.getHomeAdress() == null ? new ArrayList<>() : new ArrayList<>(request.getHomeAdress());
        customer.setHomeAddress(homeAddress);
        customer.setOtherAddress(new ArrayList<>());
        return customer;
    }

    private CustomerResponse mapToResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setIdno(customer.getIdno());
        response.setRole(customer.getRole().name());
        response.setTitle(customer.getTitle());
        response.setName(customer.getName());
        response.setSurname(customer.getSurname());
        response.setDateOfBirth(customer.getDateOfBirth());
        response.setCustomerLanguage(customer.getCustomerLanguage().name());
        response.setEthnicity(customer.getEthnicity());
        response.setGender(customer.getGender());
        response.setCountry(customer.getCountry());
        response.setHomeAdress(customer.getHomeAddress());
        response.setOtherAdress(customer.getOtherAddress());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        return response;
    }
}
