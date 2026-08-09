package com.banking.accountservice.securityConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
       
        http 
         .authorizeHttpRequests(auth -> auth.requestMatchers("/api/v1/accounts/**")
         .permitAll().anyRequest().authenticated()
       
        )
        .oauth2ResourceServer(oauth -> oauth.jwt(null));

        return http.build();
    }
}
