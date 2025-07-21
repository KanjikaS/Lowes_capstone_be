package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.*;
import com.capstone.warranty_tracker.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // Ensure this matches your frontend
public class AuthController {

    @Autowired private AuthService authService;

    @PostMapping("/register/homeowner")
    public ResponseEntity<String> registerHomeowner(@RequestBody HomeownerRegisterRequest request) {
        return authService.registerHomeowner(request);
    }

    @PostMapping("/register/technician")
    public ResponseEntity<String> registerTechnician(@RequestBody TechnicianRegisterRequest request) {
        return authService.registerTechnician(request);
    }

    // Changed return type to ResponseEntity<?> to match AuthService
    @PostMapping("/login/homeowner")
    public ResponseEntity<?> loginHomeowner(@RequestBody AuthRequest request) {
        return authService.loginAsHomeowner(request);
    }

    // Changed return type to ResponseEntity<?> to match AuthService
    @PostMapping("/login/technician")
    public ResponseEntity<?> loginTechnician(@RequestBody AuthRequest request) {
        return authService.loginAsTechnician(request);
    }

    // Changed return type to ResponseEntity<?> to match AuthService
    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestBody AuthRequest request) {
        return authService.loginAsAdmin(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        return authService.logout(request);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody String email) {
        return authService.forgotPassword(email);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request) {
        return authService.resetPassword(request);
    }
}