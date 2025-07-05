package com.capstone.warranty_tracker.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final String secret = "a-very-strong-and-secure-secret-key-of-32-characters";
    private final SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

    //
    public String generateToken(UserDetails userDetails, String email, String username) {
        return Jwts.builder()
                .setSubject(email) //
                .claim("username", username) //
                .claim("role", userDetails.getAuthorities().iterator().next().getAuthority()) // e.g., ROLE_HOMEOWNER
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //  Extract email (subject)
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    //  Extract username from claims
    public String extractUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    //  Extract role
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    //  Internal method to get claims
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //  Token validation based on email match
    public boolean validateToken(String token, UserDetails user) {
        return extractEmail(token).equals(user.getUsername()) &&
                !isTokenExpired(token); // Assuming your UserDetails loads by email
    }
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
