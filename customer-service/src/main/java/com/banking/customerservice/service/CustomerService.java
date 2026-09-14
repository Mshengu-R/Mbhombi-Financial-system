package com.banking.customerservice.service;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.banking.customerservice.dto.CustomerResponse;
import com.banking.customerservice.dto.CustomerUpdateRequest;
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
    @Transactional(readOnly = true)
    public CustomerResponse getAccount(UUID id) {
        Customer customer = findByAuthCustomerId(id);

        return mapToResponse(customer);
    }

    @Transactional // this updates user details partially
    public CustomerResponse updateAccount(UUID id, CustomerUpdateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile request is required");
        }

        Customer customer = findByAuthCustomerId(id);

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (customerRepository.existsByPhoneAndId(request.getPhone(), id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A profile already exists for this phone number");
            }
            customer.setPhone(request.getPhone());
        }

        // if (request.getName() != null) {
        //     customer.setName(request.getName());
        // }

        if (request.getSurname() != null) {
            customer.setSurname(request.getSurname());
        }

        // if (request.getDateOfBirth() != null) {
        //     customer.setDateOfBirth(request.getDateOfBirth());
        // }

        if (request.getCustomerLanguage() != null && !request.getCustomerLanguage().isBlank()) {
            customer.setCustomerLanguage(parseCustomerLanguage(request.getCustomerLanguage()));
        }

        if (request.getEthnicity() != null && !request.getEthnicity().isBlank()) {
            customer.setEthnicity(parseCustomerEthnicity(request.getEthnicity()));
        }

        if (request.getGender() != null && !request.getGender().isBlank()) {
            customer.setGender(parseCustomerGender(request.getGender()));
        }

        if (request.getHomeAdress() != null) {
            customer.setHomeAddress(new ArrayList<>(request.getHomeAdress()));
        }

        // customer.setCountry(parseCustomerCountry(request.getCountry()));

        return mapToResponse(customerRepository.save(customer));
    }

    @Transactional
    public void deleteAccount(UUID id) {
        Customer customer = findByAuthCustomerId(id);
        customerRepository.delete(customer);
    }

    private Customer findByAuthCustomerId(UUID id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated customer is required");
        }
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer profile was not found"));
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

    private CustomerResponse mapToResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        // response.setIdNumber(customer.getIdNumber());
        response.setRole(customer.getRole().name());
        response.setTitle(customer.getTitle());
        // response.setName(customer.getName());
        response.setSurname(customer.getSurname());
        // response.setDateOfBirth(customer.getDateOfBirth());
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
