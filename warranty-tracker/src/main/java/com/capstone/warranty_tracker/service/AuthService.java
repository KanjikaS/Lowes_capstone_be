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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

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

    public ResponseEntity<AuthResponse> loginAsHomeowner(AuthRequest request) {
        return loginWithRoleCheck(request, Role.ROLE_HOMEOWNER);
    }

    public ResponseEntity<AuthResponse> loginAsTechnician(AuthRequest request) {
        return loginWithRoleCheck(request, Role.ROLE_TECHNICIAN);
    }

    public ResponseEntity<AuthResponse> loginAsAdmin(AuthRequest request) {
        return loginWithRoleCheck(request, Role.ROLE_ADMIN);
    }

    private ResponseEntity<AuthResponse> loginWithRoleCheck(AuthRequest request, Role expectedRole) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.getRole().equals(expectedRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
}
