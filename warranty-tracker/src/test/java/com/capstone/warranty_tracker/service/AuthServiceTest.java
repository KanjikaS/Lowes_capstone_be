package com.capstone.warranty_tracker.service;

import static org.junit.jupiter.api.Assertions.*;

import com.capstone.warranty_tracker.dto.AuthRequest;
import com.capstone.warranty_tracker.dto.AuthResponse;
import com.capstone.warranty_tracker.dto.HomeownerRegisterRequest;
import com.capstone.warranty_tracker.dto.TechnicianRegisterRequest;
import com.capstone.warranty_tracker.model.*;
import com.capstone.warranty_tracker.repository.UserRepository;
import com.capstone.warranty_tracker.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;  // Inject mocks into AuthService

    @Mock
    private UserRepository userRepository;  // Mock UserRepository

    @Mock
    private PasswordEncoder passwordEncoder;  // Mock PasswordEncoder

    @Mock
    private JwtUtil jwtUtil;  // Mock JwtUtil

    @Mock
    private AuthenticationManager authenticationManager;  // Mock AuthenticationManager

    // Initialize mocks before each test
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks
    }

    // ==============================
    // Test for Homeowner Registration and Login
    // ==============================

    // Test Register Homeowner Success
    @Test
    public void testRegisterHomeownerSuccess() {
        HomeownerRegisterRequest request = new HomeownerRegisterRequest();
        request.setEmail("homeowner@test.com");
        request.setUsername("homeowner1");
        request.setPassword("password");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setAddress("123 Main St");
        request.setPhoneNumber("123-456-7890");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);  // Mock existsByEmail

        ResponseEntity<String> response = authService.registerHomeowner(request);

        assertEquals("Homeowner registered successfully.", response.getBody());
    }

    // Test Register Homeowner Email Already Exists
    @Test
    public void testRegisterHomeownerEmailExists() {
        HomeownerRegisterRequest request = new HomeownerRegisterRequest();
        request.setEmail("homeowner@test.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);  // Mock email already exists

        ResponseEntity<String> response = authService.registerHomeowner(request);

        assertEquals("Email already in use", response.getBody());
    }

    // Test Login Homeowner Success
    @Test
    public void testLoginHomeownerSuccess() {
        AuthRequest request = new AuthRequest();
        request.setEmail("homeowner@test.com");
        request.setPassword("password");

        User user = new Homeowner();
        user.setEmail("homeowner@test.com");
        user.setPassword(new BCryptPasswordEncoder().encode("password"));
        user.setRole(Role.ROLE_HOMEOWNER);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));  // Mock findByEmail
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("token");  // Mock JWT generation

        ResponseEntity<AuthResponse> response = authService.loginAsHomeowner(request);

        assertEquals("token", response.getBody().getToken());
    }

    // ==============================
    // Test for Technician Registration and Login
    // ==============================

    // Test Register Technician Success
    @Test
    public void testRegisterTechnicianSuccess() {
        TechnicianRegisterRequest request = new TechnicianRegisterRequest();
        request.setEmail("technician@test.com");
        request.setUsername("technician1");
        request.setPassword("password");
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setPhoneNumber("123-456-7891");
        request.setExperience(5);
        request.setSpecialization("HVAC");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);  // Mock existsByEmail

        ResponseEntity<String> response = authService.registerTechnician(request);

        assertEquals("Technician registered successfully.", response.getBody());
    }

    // Test Register Technician Email Already Exists
    @Test
    public void testRegisterTechnicianEmailExists() {
        TechnicianRegisterRequest request = new TechnicianRegisterRequest();
        request.setEmail("technician@test.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);  // Mock email already exists

        ResponseEntity<String> response = authService.registerTechnician(request);

        assertEquals("Email already in use", response.getBody());
    }

    // Test Login Technician Success
    @Test
    public void testLoginTechnicianSuccess() {
        AuthRequest request = new AuthRequest();
        request.setEmail("technician@test.com");
        request.setPassword("password");

        User user = new Technician();
        user.setEmail("technician@test.com");
        user.setPassword(new BCryptPasswordEncoder().encode("password"));
        user.setRole(Role.ROLE_TECHNICIAN);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));  // Mock findByEmail
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("token");  // Mock JWT generation

        ResponseEntity<AuthResponse> response = authService.loginAsTechnician(request);

        assertEquals("token", response.getBody().getToken());
    }

    @Test
    public void testLoginAdminSuccess() {
        AuthRequest request = new AuthRequest();
        request.setEmail("admin@test.com");
        request.setPassword("password");

        User user = new Admin();  // Assuming Admin extends User
        user.setEmail("admin@test.com");
        user.setPassword(new BCryptPasswordEncoder().encode("password")); // Encoded password
        user.setRole(Role.ROLE_ADMIN);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));  // Mock DB lookup
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("token");         // Mock JWT token generation

        ResponseEntity<AuthResponse> response = authService.loginAsAdmin(request);

        assertNotNull(response.getBody());
        assertEquals("token", response.getBody().getToken());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    // ==============================
    // Test Failure Scenarios: Incorrect Password, User Not Found, Role Mismatch
    // ==============================

    // Test Login User Not Found (Homeowner)
    @Test
    public void testLoginHomeownerUserNotFound() {
        AuthRequest request = new AuthRequest();
        request.setEmail("nonexistentuser@test.com");
        request.setPassword("password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());  // Mock user not found

        assertThrows(UsernameNotFoundException.class, () -> {
            authService.loginAsHomeowner(request);  // Should throw exception as user doesn't exist
        });
    }

    // Test Login with Incorrect Password (Homeowner)
    @Test
    public void testLoginHomeownerIncorrectPassword() {
        AuthRequest request = new AuthRequest();
        request.setEmail("homeowner@test.com");
        request.setPassword("wrong-password");

        // Simulate authentication failure
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Expect exception OR handle it gracefully in your service
        assertThrows(BadCredentialsException.class, () -> {
            authService.loginAsHomeowner(request);
        });
    }

    // Test Login Role Mismatch (Homeowner trying to log in as Technician)
    @Test
    public void testLoginRoleMismatch() {
        AuthRequest request = new AuthRequest();
        request.setEmail("homeowner@test.com");
        request.setPassword("password");

        // Create a user with ROLE_TECHNICIAN (wrong role)
        User user = new Technician();
        user.setEmail("homeowner@test.com");
        user.setPassword(new BCryptPasswordEncoder().encode("password"));
        user.setRole(Role.ROLE_TECHNICIAN);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));  // Mock findByEmail

        // Expect a Forbidden response due to role mismatch
        ResponseEntity<AuthResponse> response = authService.loginAsHomeowner(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());  // Status should be 403 Forbidden
    }

    // Test Logout
    @Test
    public void testLogout() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<String> response = authService.logout(request);

        assertEquals("Logout successful", response.getBody());
    }
}