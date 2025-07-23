package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.*;
import com.capstone.warranty_tracker.model.ServiceStatus;
import com.capstone.warranty_tracker.security.JwtUtil;
import com.capstone.warranty_tracker.service.TechnicianService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // no JWT filter for this unit test
class TechnicianControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TechnicianService technicianService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "tech@example.com", roles = "TECHNICIAN")
    void testGetAssignedRequests() throws Exception {
        when(technicianService.getAssignedRequestsForTechnician(any()))
                .thenReturn(List.of(new ServiceRequestResponseDto()));

        mockMvc.perform(get("/technician/assigned-requests")
                        .principal(() -> "tech@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(technicianService).getAssignedRequestsForTechnician("tech@example.com");
    }

    @Test
    @WithMockUser(username = "tech@example.com", roles = "TECHNICIAN")
    void testUpdateRequestStatus() throws Exception {
        UpdateRequestStatusDto updateDto = new UpdateRequestStatusDto();
        updateDto.setRequestId(1L);
        updateDto.setStatus(ServiceStatus.IN_PROGRESS);

        String json = objectMapper.writeValueAsString(updateDto);

        doNothing().when(technicianService).updateRequestStatus(eq("tech@example.com"), any(UpdateRequestStatusDto.class));

        mockMvc.perform(put("/technician/update-status")
                        .principal(() -> "tech@example.com")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Service request status updated successfully."));
    }

    @Test
    @WithMockUser(username = "tech@example.com", roles = "TECHNICIAN")
    void testGetInProgressRequests() throws Exception {
        when(jwtUtil.extractEmail(any())).thenReturn("tech@example.com");
        when(technicianService.getInProgressRequestsForTechnician(any()))
                .thenReturn(List.of(new ServiceRequestResponseDto()));

        mockMvc.perform(get("/technician/requests/in-progress")
                        .header("Authorization", "Bearer dummyToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "tech@example.com", roles = "TECHNICIAN")
    void testGetCompletedRequests() throws Exception {
        when(jwtUtil.extractEmail(any())).thenReturn("tech@example.com");
        when(technicianService.getCompletedRequestsForTechnician(any()))
                .thenReturn(List.of(new ServiceRequestResponseDto()));

        mockMvc.perform(get("/technician/requests/completed")
                        .header("Authorization", "Bearer dummyToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "tech@example.com", roles = "TECHNICIAN")
    void testGetTechnicianProfile() throws Exception {
        TechnicianResponseDto profile = TechnicianResponseDto.builder()
                .id(1L)
                .firstName("Tech")
                .lastName("User")
                .email("tech@example.com")
                .phoneNumber("1234567890")
                .specialization("Electronics")
                .experience(5)
                .build();

        when(technicianService.getTechnicianProfile(any())).thenReturn(profile);

        mockMvc.perform(get("/technician/profile")
                        .principal(() -> "tech@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("tech@example.com"));
    }

    @Test
    @WithMockUser(username = "tech@example.com", roles = "TECHNICIAN")
    void testGetTechnicianStats() throws Exception {
        TechnicianStatsDto stats = TechnicianStatsDto.builder()
                .assignedCount(2)
                .inProgressCount(1)
                .completedCount(3)
                .build();

        when(technicianService.getTechnicianStats(any())).thenReturn(stats);

        mockMvc.perform(get("/technician/stats")
                        .principal(() -> "tech@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedCount").value(2));
    }

    @Test
    @WithMockUser(username = "tech@example.com", roles = "TECHNICIAN")
    void testSubmitCompletionForm() throws Exception {
        CompletionFormDto dto = CompletionFormDto.builder()
                .completionDate("2025-07-04")
                .completionTime("10:30")
                .technicianNotes("Job done well.")
                .confirmed(true)
                .build();

        String json = objectMapper.writeValueAsString(dto);

        doNothing().when(technicianService).saveCompletionForm(eq(1L), any(CompletionFormDto.class), eq("tech@example.com"));

        mockMvc.perform(post("/technician/service-request/1/completion")
                        .principal(() -> "tech@example.com")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Completion form submitted successfully."));
    }

    @Test
    @WithMockUser(username = "tech@example.com", roles = "TECHNICIAN")
    void testGetCompletionForm() throws Exception {
        CompletionFormResponseDto responseDto = new CompletionFormResponseDto();
        responseDto.setId(1L);
        responseDto.setCompletionDate("2025-07-04");
        responseDto.setCompletionTime("10:30");
        responseDto.setTechnicianNotes("Completed work.");
        responseDto.setConfirmed(true);
        responseDto.setServiceRequestId(1L);
        responseDto.setTechnicianId(1L);
        responseDto.setTechnicianEmail("tech@example.com");

        when(technicianService.getCompletionForm(eq(1L), eq("tech@example.com")))
                .thenReturn(responseDto);

        mockMvc.perform(get("/technician/service-request/1/completion")
                        .principal(() -> "tech@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technicianEmail").value("tech@example.com"));
    }
}
