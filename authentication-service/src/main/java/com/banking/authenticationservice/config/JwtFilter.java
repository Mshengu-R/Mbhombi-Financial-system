package com.banking.authenticationservice.config;

// import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
// import com.banking.authenticationservice.entity.UserCredentials;
// import com.banking.authenticationservice.repository.UserCredentialsRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import com.banking.authenticationservice.service.CustomerDetailsService;
import com.banking.authenticationservice.service.JWTService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter{

    private final JWTService jwtService;
    private final CustomerDetailsService customerDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response, 
                                     FilterChain filterChain) 
                                     throws ServletException, java.io.IOException {
                                        String authHeader = request.getHeader("Authorization");
                                        String token = null;
                                        String username = null;

            // this for grabbing token
          if(authHeader != null && authHeader.startsWith("Bearer ")){
                      token = authHeader.substring(7);
                      try {
                          username = jwtService.extractUsername(token);
                      } catch (RuntimeException ignored) {
                          filterChain.doFilter(request, response);
                          return;
                      }
                  } 
                    
             if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

              UserDetails userDetails;
              try {
                  userDetails = customerDetailsService.loadUserByUsername(username);
              } catch (RuntimeException ignored) {
                  filterChain.doFilter(request, response);
                  return;
              }

              if(jwtService.validateToken(token, userDetails)) {
                     UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                                                                                                 null,
                                                                                                 userDetails.getAuthorities());
                                                                                                 
                     authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                     SecurityContextHolder.getContext().setAuthentication(authToken);

                    }
                        
                    }

              filterChain.doFilter(request, response);                
                        
     }

}
