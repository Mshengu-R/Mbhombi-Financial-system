package com.banking.authenticationservice.service;

import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
// import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jws;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.io.Decoder;
import io.jsonwebtoken.io.Decoders;

// import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

@Service
public class JWTService {
    
    @Value("${app.jwt.secret}")
    private String SECRET_KEY;
    


/*
*  JWT Service for token generation and validation
*/

public JWTService() {

    try {
 
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSha256");
        SecretKey sk = keyGen.generateKey();
        SECRET_KEY = Base64.getEncoder().encodeToString(sk.getEncoded());

    } catch (Exception e) {

        throw new RuntimeException(e);
    }
}


public String generateToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    return Jwts.builder()
    .claims()
    .add(claims)
    .subject(username)
    .issuedAt(new Date(System.currentTimeMillis()))
    .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 60))
    .and()
    .signWith(getKey())
    .compact();
}


private SecretKey getKey() {

    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
    return Keys.hmacShaKeyFor(keyBytes);
}

public String extractUserName(String token) {
    return extractClaim(token, Claims::getSubject);
}


private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
}


private Claims extractAllClaims(String token) {

        return Jwts
        .parser()
        .verifyWith(getKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();

}

public boolean validateToken(String token, UserDetails userDetails) {
    final String userName = extractUserName(token);
    return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
}

private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
}


private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
}



}
