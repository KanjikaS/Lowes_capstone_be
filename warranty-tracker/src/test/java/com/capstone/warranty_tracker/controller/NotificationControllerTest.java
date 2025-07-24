package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.repository.ApplianceRepository;
import com.capstone.warranty_tracker.security.JwtFilter;
import com.capstone.warranty_tracker.security.JwtUtil;
import com.capstone.warranty_tracker.service.WarrantyExpirySchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class NotificationControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private WarrantyExpirySchedulerService schedulerService;

    @MockBean
    private ApplianceRepository applianceRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testTriggerWarrantyCheck() throws Exception {
        doNothing().when(schedulerService).triggerManualWarrantyCheck();

        mockMvc.perform(post("/api/notifications/check-warranty-expiry"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testTriggerWarrantyCheckInDays() throws Exception {
        int days = 7;
        doNothing().when(schedulerService).checkWarrantyExpiryInDays(days);

        mockMvc.perform(post("/api/notifications/check-warranty-expiry/{days}", days))
                .andExpect(status().isOk());
    }
} 