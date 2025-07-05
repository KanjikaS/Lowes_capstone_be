package com.capstone.warranty_tracker.service;

import static org.junit.jupiter.api.Assertions.*;

import com.capstone.warranty_tracker.model.Role;
import com.capstone.warranty_tracker.model.User;
import com.capstone.warranty_tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;  // Mock UserRepository

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;  // Inject the mock into the service

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks

        // Prepare a mock User object for testing
        mockUser = new User();
        mockUser.setEmail("user@example.com");
        mockUser.setPassword("password");
        mockUser.setRole(Role.ROLE_HOMEOWNER);  // Assume this user is a homeowner
    }

    @Test
    void loadUserByUsername_Success() {
        // Arrange
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(java.util.Optional.of(mockUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(mockUser.getEmail());

        // Assert
        assertNotNull(userDetails);  // Ensure userDetails is not null
        assertEquals(mockUser.getEmail(), userDetails.getUsername());  // Check that the username (email) matches
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(mockUser.getRole().name())));  // Verify that the correct role is set
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.empty());  // Return empty Optional

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(email));  // Expect exception
    }
}