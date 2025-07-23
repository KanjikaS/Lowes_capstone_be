package com.capstone.warranty_tracker.controller;
import com.capstone.warranty_tracker.dto.*;
import com.capstone.warranty_tracker.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest httpServletRequest; // Mock HttpServletRequest

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerHomeowner_Success() {
        HomeownerRegisterRequest request = new HomeownerRegisterRequest();
        when(authService.registerHomeowner(request)).thenReturn(ResponseEntity.ok("Homeowner registered successfully."));

        ResponseEntity<String> response = authController.registerHomeowner(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Homeowner registered successfully.", response.getBody());
        verify(authService, times(1)).registerHomeowner(request);
    }

    @Test
    void registerTechnician_Success() {
        TechnicianRegisterRequest request = new TechnicianRegisterRequest();
        when(authService.registerTechnician(request)).thenReturn(ResponseEntity.ok("Technician registered successfully."));

        ResponseEntity<String> response = authController.registerTechnician(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Technician registered successfully.", response.getBody());
        verify(authService, times(1)).registerTechnician(request);
    }

    @Test
    void loginHomeowner_Success() {
        AuthRequest request = new AuthRequest("test@home.com", "password");
        AuthResponse authResponse = new AuthResponse("jwt.token", "ROLE_HOMEOWNER", "testuser");
        // FIX: Cast the ResponseEntity.ok() result to ResponseEntity
        when(authService.loginAsHomeowner(request)).thenReturn((ResponseEntity) ResponseEntity.ok(authResponse));

        ResponseEntity<?> response = authController.loginHomeowner(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());
        verify(authService, times(1)).loginAsHomeowner(request);
    }

    @Test
    void loginTechnician_Success() {
        AuthRequest request = new AuthRequest("test@tech.com", "password");
        AuthResponse authResponse = new AuthResponse("jwt.token", "ROLE_TECHNICIAN", "techuser");
        // FIX: Cast the ResponseEntity.ok() result to ResponseEntity
        when(authService.loginAsTechnician(request)).thenReturn((ResponseEntity) ResponseEntity.ok(authResponse));

        ResponseEntity<?> response = authController.loginTechnician(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());
        verify(authService, times(1)).loginAsTechnician(request);
    }

    @Test
    void loginAdmin_Success() {
        AuthRequest request = new AuthRequest("test@admin.com", "password");
        AuthResponse authResponse = new AuthResponse("jwt.token", "ROLE_ADMIN", "adminuser");
        // FIX: Cast the ResponseEntity.ok() result to ResponseEntity
        when(authService.loginAsAdmin(request)).thenReturn((ResponseEntity) ResponseEntity.ok(authResponse));

        ResponseEntity<?> response = authController.loginAdmin(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());
        verify(authService, times(1)).loginAsAdmin(request);
    }

    @Test
    void logout_Success() {
        when(authService.logout(httpServletRequest)).thenReturn(ResponseEntity.ok("Logout successful"));

        ResponseEntity<String> response = authController.logout(httpServletRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successful", response.getBody());
        verify(authService, times(1)).logout(httpServletRequest);
    }

    @Test
    void forgotPassword_Success() {
        String email = "test@example.com";
        when(authService.forgotPassword(email)).thenReturn(ResponseEntity.ok("Password reset link sent."));

        ResponseEntity<String> response = authController.forgotPassword(email);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset link sent.", response.getBody());
        verify(authService, times(1)).forgotPassword(email);
    }

    @Test
    void resetPassword_Success() {
        PasswordResetRequest request = new PasswordResetRequest();
        when(authService.resetPassword(request)).thenReturn(ResponseEntity.ok("Password reset successfully."));

        ResponseEntity<String> response = authController.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successfully.", response.getBody());
        verify(authService, times(1)).resetPassword(request);
    }
}