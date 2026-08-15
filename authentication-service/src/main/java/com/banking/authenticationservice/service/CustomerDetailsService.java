package com.banking.authenticationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.banking.authenticationservice.entity.Customer;
import com.banking.authenticationservice.repository.UserCredentialsRepository;
 
@Service
public class CustomerDetailsService  implements UserDetailsService{
    
    @Autowired
    private UserCredentialsRepository userCredentialsRepository;


  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
   Customer user = userCredentialsRepository.findByUsername(username);
        return (UserDetails) user;
    }

}
