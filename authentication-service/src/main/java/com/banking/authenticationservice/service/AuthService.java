package com.banking.authenticationservice.service;

import com.banking.authenticationservice.dto.AuthorizationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

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
    
    @Autowired
    private UserCredentialsRepository repository;

    @Autowired
    // @Bean
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    
    @Autowired
    AuthenticationManager authManager;

    @Autowired
    private JWTService jwtService;

    public String saveUser(Customer credentials){
        credentials.setPassword(encoder.encode(credentials.getPassword()));
        repository.save(credentials);
        return "You credentials have been saved";

    }



    // method to validate the token
    public String validateToken(Customer user) {
        Authentication authentication = authManager
                        .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
    
                        if(authentication.isAuthenticated()) {
                            return jwtService.generateToken(user.getUsername());
                        }
                        else {
                            return "Fail";
                        }
                    }


public Customer buildCustomer(AuthorizationRequest request,
                            CustomerRole role,
                            CustomerCountry country,
                            CustomerTitle title,
                            CustomerLanguage lang,
                            CustomerGender gender,
                            CustomerEthnicity ethnicity) {
                          
    Customer customer = new Customer();
    customer.setEmail(request.getEmail());
    customer.setName(request.getName());
    customer.setSurname(request.getSurname());
    customer.setPhone(request.getPhone());
    customer.setTitle(title);
    customer.setCountry(country);
    customer.setGender(gender);
    customer.setCustomerLanguage(lang);
    customer.seEthnicity(ethnicity);
    return customer;

    
}
}