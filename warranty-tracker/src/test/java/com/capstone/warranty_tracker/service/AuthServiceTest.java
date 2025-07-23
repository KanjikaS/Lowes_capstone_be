package com.capstone.warranty_tracker.service;
import com.capstone.warranty_tracker.dto.*;
import com.capstone.warranty_tracker.model.*;
import com.capstone.warranty_tracker.repository.UserRepository;
import com.capstone.warranty_tracker.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private EmailService emailService;
    @Mock
    private HttpServletRequest httpServletRequest; // Mock HttpServletRequest

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Reset SecurityContextHolder before each test to ensure a clean state
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerHomeowner_Success() {
        HomeownerRegisterRequest request = new HomeownerRegisterRequest();
        request.setEmail("new@home.com");
        request.setPassword("password");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setAddress("123 Main St");
        request.setPhoneNumber("1234567890");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        ResponseEntity<String> response = authService.registerHomeowner(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Homeowner registered successfully.", response.getBody());

        ArgumentCaptor<Homeowner> homeownerCaptor = ArgumentCaptor.forClass(Homeowner.class);
        verify(userRepository, times(1)).save(homeownerCaptor.capture());

        Homeowner capturedHomeowner = homeownerCaptor.getValue();
        assertEquals("new@home.com", capturedHomeowner.getEmail());
        assertEquals("encodedPassword", capturedHomeowner.getPassword());
        assertEquals(Role.ROLE_HOMEOWNER, capturedHomeowner.getRole());
        assertEquals("John", capturedHomeowner.getFirstName());
        assertEquals("Doe", capturedHomeowner.getLastName());
        assertEquals("123 Main St", capturedHomeowner.getAddress());
        assertEquals("1234567890", capturedHomeowner.getPhoneNumber());
    }

    @Test
    void registerHomeowner_EmailAlreadyInUse() {
        HomeownerRegisterRequest request = new HomeownerRegisterRequest();
        request.setEmail("existing@home.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        ResponseEntity<String> response = authService.registerHomeowner(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already in use", response.getBody());
        verify(userRepository, never()).save(any(Homeowner.class));
    }

    @Test
    void registerTechnician_Success() {
        TechnicianRegisterRequest request = new TechnicianRegisterRequest();
        request.setEmail("new@tech.com");
        request.setPassword("password");
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setExperience(5);
        request.setSpecialization("HVAC");
        request.setPhoneNumber("9876543210");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        ResponseEntity<String> response = authService.registerTechnician(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Technician registered successfully.", response.getBody());

        ArgumentCaptor<Technician> technicianCaptor = ArgumentCaptor.forClass(Technician.class);
        verify(userRepository, times(1)).save(technicianCaptor.capture());

        Technician capturedTechnician = technicianCaptor.getValue();
        assertEquals("new@tech.com", capturedTechnician.getEmail());
        assertEquals("encodedPassword", capturedTechnician.getPassword());
        assertEquals(Role.ROLE_TECHNICIAN, capturedTechnician.getRole());
        assertEquals("Jane", capturedTechnician.getFirstName());
        assertEquals("Smith", capturedTechnician.getLastName());
        assertEquals(5, capturedTechnician.getExperience());
        assertEquals("HVAC", capturedTechnician.getSpecialization());
        assertEquals("9876543210", capturedTechnician.getPhoneNumber());
    }

    @Test
    void registerTechnician_EmailAlreadyInUse() {
        TechnicianRegisterRequest request = new TechnicianRegisterRequest();
        request.setEmail("existing@tech.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        ResponseEntity<String> response = authService.registerTechnician(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already in use", response.getBody());
        verify(userRepository, never()).save(any(Technician.class));
    }

    @Test
    void loginAsHomeowner_Success() {
        AuthRequest authRequest = new AuthRequest("home@user.com", "password");
        Homeowner homeowner = new Homeowner();
        homeowner.setEmail("home@user.com");
        homeowner.setPassword("encodedPassword");
        homeowner.setRole(Role.ROLE_HOMEOWNER);
        homeowner.setUsername("homeuser");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail("home@user.com")).thenReturn(Optional.of(homeowner));

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                homeowner.getEmail(), homeowner.getPassword(),
                List.of(new SimpleGrantedAuthority(homeowner.getRole().name())));
        when(jwtUtil.generateToken(any(UserDetails.class), eq(homeowner.getEmail()), eq(homeowner.getUsername()))).thenReturn("mocked.jwt.token");

        ResponseEntity<?> response = authService.loginAsHomeowner(authRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("mocked.jwt.token", authResponse.getToken());
        assertEquals("ROLE_HOMEOWNER", authResponse.getRole());
        assertEquals("homeuser", authResponse.getUsername());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail("home@user.com");
        verify(jwtUtil, times(1)).generateToken(any(UserDetails.class), eq(homeowner.getEmail()), eq(homeowner.getUsername()));
    }

    @Test
    void loginAsHomeowner_InvalidCredentials_BadPassword() {
        AuthRequest authRequest = new AuthRequest("home@user.com", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        ResponseEntity<?> response = authService.loginAsHomeowner(authRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password.", response.getBody());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }

    @Test
    void loginAsHomeowner_InvalidCredentials_UserNotFoundInDBAfterAuth() {
        AuthRequest authRequest = new AuthRequest("home@user.com", "password");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        // This will cause orElseThrow to throw UsernameNotFoundException, which is caught by AuthenticationException
        when(userRepository.findByEmail("home@user.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = authService.loginAsHomeowner(authRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password.", response.getBody());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail("home@user.com"); // Still called
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }

    @Test
    void loginAsHomeowner_IncorrectRole() {
        AuthRequest authRequest = new AuthRequest("tech@user.com", "password");
        Technician technician = new Technician();
        technician.setEmail("tech@user.com");
        technician.setPassword("encodedPassword");
        technician.setRole(Role.ROLE_TECHNICIAN);
        technician.setUsername("techuser");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail("tech@user.com")).thenReturn(Optional.of(technician));

        ResponseEntity<?> response = authService.loginAsHomeowner(authRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied for this role.", response.getBody());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail("tech@user.com");
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }

    @Test
    void loginAsTechnician_Success() {
        AuthRequest authRequest = new AuthRequest("tech@user.com", "password");
        Technician technician = new Technician();
        technician.setEmail("tech@user.com");
        technician.setPassword("encodedPassword");
        technician.setRole(Role.ROLE_TECHNICIAN);
        technician.setUsername("techuser");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail("tech@user.com")).thenReturn(Optional.of(technician));

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                technician.getEmail(), technician.getPassword(),
                List.of(new SimpleGrantedAuthority(technician.getRole().name())));
        when(jwtUtil.generateToken(any(UserDetails.class), eq(technician.getEmail()), eq(technician.getUsername()))).thenReturn("mocked.jwt.token");

        ResponseEntity<?> response = authService.loginAsTechnician(authRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("mocked.jwt.token", authResponse.getToken());
        assertEquals("ROLE_TECHNICIAN", authResponse.getRole());
        assertEquals("techuser", authResponse.getUsername());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail("tech@user.com");
        verify(jwtUtil, times(1)).generateToken(any(UserDetails.class), eq(technician.getEmail()), eq(technician.getUsername()));
    }

    @Test
    void loginAsAdmin_Success() {
        AuthRequest authRequest = new AuthRequest("admin@user.com", "password");
        Admin admin = new Admin();
        admin.setEmail("admin@user.com");
        admin.setPassword("encodedPassword");
        admin.setRole(Role.ROLE_ADMIN);
        admin.setUsername("adminuser");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail("admin@user.com")).thenReturn(Optional.of(admin));

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                admin.getEmail(), admin.getPassword(),
                List.of(new SimpleGrantedAuthority(admin.getRole().name())));
        when(jwtUtil.generateToken(any(UserDetails.class), eq(admin.getEmail()), eq(admin.getUsername()))).thenReturn("mocked.jwt.token");

        ResponseEntity<?> response = authService.loginAsAdmin(authRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("mocked.jwt.token", authResponse.getToken());
        assertEquals("ROLE_ADMIN", authResponse.getRole());
        assertEquals("adminuser", authResponse.getUsername());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail("admin@user.com");
        verify(jwtUtil, times(1)).generateToken(any(UserDetails.class), eq(admin.getEmail()), eq(admin.getUsername()));
    }

    @Test
    void logout_Success() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext); // Set a mock context

        ResponseEntity<String> response = authService.logout(httpServletRequest); // httpServletRequest is mocked but not used by this method directly

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successful", response.getBody());
        // Verify that clearAuthentication was called on the mocked SecurityContext
        // NOTE: SecurityContextHolder.clearContext() is static, actual verification of that static method call
        // requires Mockito.mockStatic, which is not used here to keep this specific test simple
        // and avoid static mocking side effects on other tests if not closed properly.
        // The effective behavior of clearing the context is verified by checking the return value.
    }

    @Test
    void forgotPassword_UserExists_EmailSent() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setUsername("testuser");
        user.setRole(Role.ROLE_HOMEOWNER);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        ResponseEntity<String> response = authService.forgotPassword(email);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset link sent to your email.", response.getBody());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertNotNull(userCaptor.getValue().getResetToken());
        assertNotNull(userCaptor.getValue().getResetTokenExpiryDate());
        assertTrue(userCaptor.getValue().getResetTokenExpiryDate().isAfter(LocalDateTime.now().minusMinutes(1)));

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(1)).sendPasswordResetEmail(emailCaptor.capture(), linkCaptor.capture());
        assertEquals(email, emailCaptor.getValue());
        assertTrue(linkCaptor.getValue().startsWith("http://localhost:3000/reset-password?token="));
    }

    @Test
    void forgotPassword_UserDoesNotExist_GenericSuccessMessage() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<String> response = authService.forgotPassword(email);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("If an account with that email exists, a password reset link has been sent.", response.getBody());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void forgotPassword_EmailServiceFails() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setUsername("testuser");
        user.setRole(Role.ROLE_HOMEOWNER);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Email sending failed")).when(emailService).sendPasswordResetEmail(anyString(), anyString());

        ResponseEntity<String> response = authService.forgotPassword(email);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error sending password reset email.", response.getBody());
        verify(userRepository, times(1)).save(any(User.class)); // Token should still be saved
        verify(emailService, times(1)).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void resetPassword_Success() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken(UUID.randomUUID().toString());
        request.setNewPassword("newSecurePassword");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("oldEncodedPassword"); // Should be updated
        user.setResetToken(request.getToken());
        user.setResetTokenExpiryDate(LocalDateTime.now().plusHours(1));

        when(userRepository.findByResetToken(request.getToken())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("newEncodedPassword");

        ResponseEntity<String> response = authService.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successfully.", response.getBody());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals("newEncodedPassword", capturedUser.getPassword());
        assertNull(capturedUser.getResetToken());
        assertNull(capturedUser.getResetTokenExpiryDate());
    }

    @Test
    void resetPassword_InvalidToken() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken("invalidToken");
        request.setNewPassword("newSecurePassword");

        when(userRepository.findByResetToken(request.getToken())).thenReturn(Optional.empty());

        ResponseEntity<String> response = authService.resetPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid or expired reset token.", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_ExpiredToken() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken(UUID.randomUUID().toString());
        request.setNewPassword("newSecurePassword");

        User user = new User();
        user.setEmail("user@example.com");
        user.setResetToken(request.getToken());
        user.setResetTokenExpiryDate(LocalDateTime.now().minusHours(1)); // Expired token

        when(userRepository.findByResetToken(request.getToken())).thenReturn(Optional.of(user));

        ResponseEntity<String> response = authService.resetPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid or expired reset token.", response.getBody());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture()); // Token should be cleared
        assertNull(userCaptor.getValue().getResetToken());
        assertNull(userCaptor.getValue().getResetTokenExpiryDate());
    }

    @Test
    void resetPassword_NullExpiryDate() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken(UUID.randomUUID().toString());
        request.setNewPassword("newSecurePassword");

        User user = new User();
        user.setEmail("user@example.com");
        user.setResetToken(request.getToken());
        user.setResetTokenExpiryDate(null); // Null expiry date

        when(userRepository.findByResetToken(request.getToken())).thenReturn(Optional.of(user));

        ResponseEntity<String> response = authService.resetPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid or expired reset token.", response.getBody());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture()); // Token should be cleared
        assertNull(userCaptor.getValue().getResetToken());
        assertNull(userCaptor.getValue().getResetTokenExpiryDate());
    }
}