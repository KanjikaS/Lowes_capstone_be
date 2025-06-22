package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.*;
import com.capstone.warranty_tracker.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
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

    @PostMapping("/login/homeowner")
    public ResponseEntity<AuthResponse> loginHomeowner(@RequestBody AuthRequest request) {
        return authService.loginAsHomeowner(request);
    }

    @PostMapping("/login/technician")
    public ResponseEntity<AuthResponse> loginTechnician(@RequestBody AuthRequest request) {
        return authService.loginAsTechnician(request);
    }

    @PostMapping("/login/admin")
    public ResponseEntity<AuthResponse> loginAdmin(@RequestBody AuthRequest request) {
        return authService.loginAsAdmin(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        return authService.logout(request);
    }
}