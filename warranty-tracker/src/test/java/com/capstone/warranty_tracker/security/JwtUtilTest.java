package com.capstone.warranty_tracker.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private UserDetails userDetails;
    private String token;

    @BeforeEach
    void setUp() {
        userDetails = new org.springframework.security.core.userdetails.User(
                "user@example.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_HOMEOWNER"))
        );
        token = jwtUtil.generateToken(userDetails, "user@example.com", "user123");
    }

    @Test
    void testGenerateToken() {
        assertNotNull(token);
        assertTrue(token.startsWith("ey"));
    }

    @Test
    void testExtractEmail() {
        String email = jwtUtil.extractEmail(token);
        assertEquals("user@example.com", email);
    }

    @Test
    void testExtractUsername() {
        String username = jwtUtil.extractUsername(token);
        assertEquals("user123", username);
    }

    @Test
    void testExtractRole() {
        String role = jwtUtil.extractRole(token);
        assertEquals("ROLE_HOMEOWNER", role);
    }

    @Test
    void testValidateToken_Valid() {
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void testValidateToken_Invalid() {
        UserDetails anotherUser = new org.springframework.security.core.userdetails.User(
                "another@example.com", "pass", List.of());
        assertFalse(jwtUtil.validateToken(token, anotherUser));
    }
}