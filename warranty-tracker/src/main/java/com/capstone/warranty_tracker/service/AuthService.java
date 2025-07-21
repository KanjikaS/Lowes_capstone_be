package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.*;
import com.capstone.warranty_tracker.model.*;
import com.capstone.warranty_tracker.repository.UserRepository;
import com.capstone.warranty_tracker.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class AuthService {
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private EmailService emailService;

    public ResponseEntity<String> registerHomeowner(HomeownerRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already in use");
        }
        Homeowner homeowner = new Homeowner();
        homeowner.setUsername(request.getUsername());
        homeowner.setEmail(request.getEmail());
        homeowner.setPassword(passwordEncoder.encode(request.getPassword()));
        homeowner.setRole(Role.ROLE_HOMEOWNER);
        homeowner.setAddress(request.getAddress());
        homeowner.setPhoneNumber(request.getPhoneNumber());
        homeowner.setFirstName(request.getFirstName());
        homeowner.setLastName(request.getLastName());

        userRepository.save(homeowner);
        return ResponseEntity.ok("Homeowner registered successfully.");
    }

    public ResponseEntity<String> registerTechnician(TechnicianRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already in use");
        }
        Technician technician = new Technician();
        technician.setUsername(request.getUsername());
        technician.setEmail(request.getEmail());
        technician.setPassword(passwordEncoder.encode(request.getPassword()));
        technician.setRole(Role.ROLE_TECHNICIAN);
        technician.setPhoneNumber(request.getPhoneNumber());
        technician.setExperience(request.getExperience());
        technician.setSpecialization(request.getSpecialization());
        technician.setFirstName(request.getFirstName());
        technician.setLastName(request.getLastName());

        userRepository.save(technician);
        return ResponseEntity.ok("Technician registered successfully.");
    }

    // Changed return type to ResponseEntity<?> to handle both AuthResponse and String
    public ResponseEntity<?> loginAsHomeowner(AuthRequest request) {
        return loginWithRoleCheck(request, Role.ROLE_HOMEOWNER);
    }

    public ResponseEntity<?> loginAsTechnician(AuthRequest request) {
        return loginWithRoleCheck(request, Role.ROLE_TECHNICIAN);
    }

    public ResponseEntity<?> loginAsAdmin(AuthRequest request) {
        return loginWithRoleCheck(request, Role.ROLE_ADMIN);
    }

    private ResponseEntity<?> loginWithRoleCheck(AuthRequest request, Role expectedRole) {
        User user;
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } catch (AuthenticationException e) {
            // For security, return a generic message for invalid credentials
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
        }

        if (!user.getRole().equals(expectedRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied for this role.");
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name())));

        String token = jwtUtil.generateToken(userDetails, user.getEmail(), user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, user.getRole().name(), user.getUsername()));
    }

    public ResponseEntity<String> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logout successful");
    }

    public ResponseEntity<String> forgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            // For security, always return a generic success message even if email doesn't exist
            return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
        }

        User user = userOptional.get();
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1); // Token valid for 1 hour

        user.setResetToken(resetToken);
        user.setResetTokenExpiryDate(expiryDate);
        userRepository.save(user);

        // Construct the reset link for the frontend
        // IMPORTANT: Replace with your actual frontend URL for the password reset page
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
            return ResponseEntity.ok("Password reset link sent to your email.");
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("Error sending email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending password reset email.");
        }
    }

    public ResponseEntity<String> resetPassword(PasswordResetRequest request) {
        Optional<User> userOptional = userRepository.findByResetToken(request.getToken());

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid or expired reset token.");
        }

        User user = userOptional.get();

        if (user.getResetTokenExpiryDate() == null || user.getResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            user.setResetToken(null); // Clear expired token
            user.setResetTokenExpiryDate(null);
            userRepository.save(user);
            return ResponseEntity.badRequest().body("Invalid or expired reset token.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null); // Clear token after successful reset
        user.setResetTokenExpiryDate(null);
        userRepository.save(user);

        return ResponseEntity.ok("Password reset successfully.");
    }
}
