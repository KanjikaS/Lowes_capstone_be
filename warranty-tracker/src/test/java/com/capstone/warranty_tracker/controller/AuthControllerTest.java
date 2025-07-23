package com.capstone.warranty_tracker.controller;

import static org.junit.jupiter.api.Assertions.*;
import com.capstone.warranty_tracker.dto.AuthRequest;
import com.capstone.warranty_tracker.dto.AuthResponse;
import com.capstone.warranty_tracker.dto.HomeownerRegisterRequest;
import com.capstone.warranty_tracker.dto.TechnicianRegisterRequest;
import com.capstone.warranty_tracker.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;  // Mock the AuthService

    @InjectMocks
    private AuthController authController;  // Inject mocks into the controller

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();  // Setup MockMvc with the controller
    }

    @Test @DisplayName("POST /api/auth/register/homeowner → 200")
    void registerHomeowner_success() throws Exception {
        var req = new HomeownerRegisterRequest("u","u@h.com","pw","First","Last","123","Addr");
        when(authService.registerHomeowner(any())).thenReturn(ResponseEntity.ok("OK"));

        mockMvc.perform(post("/api/auth/register/homeowner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test @DisplayName("POST /api/auth/register/technician → 200")
    void registerTechnician_success() throws Exception {
        var req = new TechnicianRegisterRequest("t","t@h.com","pw","A","B","999","Spec",5);
        when(authService.registerTechnician(any())).thenReturn(ResponseEntity.ok("OK"));

        mockMvc.perform(post("/api/auth/register/technician")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test @DisplayName("POST /api/auth/login/homeowner → 200")
    void loginHomeowner_success() throws Exception {
        var req = new AuthRequest("u@h.com","pw");
        var resp = new AuthResponse("tok","ROLE_HOMEOWNER","u");
        when(authService.loginAsHomeowner(any())).thenReturn(ResponseEntity.ok(resp));

        mockMvc.perform(post("/api/auth/login/homeowner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(resp)));
    }

    @Test @DisplayName("POST /api/auth/login/homeowner → 403")
    void loginHomeowner_forbidden() throws Exception {
        var req = new AuthRequest("bad@h.com","pw");
        when(authService.loginAsHomeowner(any()))
                .thenReturn(ResponseEntity.status(403).build());

        mockMvc.perform(post("/api/auth/login/homeowner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /api/auth/login/technician → 200")
    void loginTechnician_success() throws Exception {
        var req = new AuthRequest("t@h.com","pw");
        var resp = new AuthResponse("tok2","ROLE_TECHNICIAN","t");
        when(authService.loginAsTechnician(any())).thenReturn(ResponseEntity.ok(resp));

        mockMvc.perform(post("/api/auth/login/technician")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(resp)));
    }

    @Test @DisplayName("POST /api/auth/login/technician → 403")
    void loginTechnician_forbidden() throws Exception {
        var req = new AuthRequest("bad@t.com","pw");
        when(authService.loginAsTechnician(any()))
                .thenReturn(ResponseEntity.status(403).build());

        mockMvc.perform(post("/api/auth/login/technician")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /api/auth/login/admin → 200")
    void loginAdmin_success() throws Exception {
        var req = new AuthRequest("a@d.com","pw");
        var resp = new AuthResponse("tok3","ROLE_ADMIN","a");
        when(authService.loginAsAdmin(any())).thenReturn(ResponseEntity.ok(resp));

        mockMvc.perform(post("/api/auth/login/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(resp)));
    }

    @Test @DisplayName("POST /api/auth/login/admin → 403")
    void loginAdmin_forbidden() throws Exception {
        var req = new AuthRequest("bad@d.com","pw");
        when(authService.loginAsAdmin(any()))
                .thenReturn(ResponseEntity.status(403).build());

        mockMvc.perform(post("/api/auth/login/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /api/auth/logout → 200")
    void logout_success() throws Exception {
        when(authService.logout(any(HttpServletRequest.class)))
                .thenReturn(ResponseEntity.ok("BYE"));

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("BYE"));
    }
}
