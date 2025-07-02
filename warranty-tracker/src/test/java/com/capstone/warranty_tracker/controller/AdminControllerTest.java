package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.AdminStatsDto;
import com.capstone.warranty_tracker.dto.ServiceRequestAdminDto;
import com.capstone.warranty_tracker.dto.TechnicianResponseDto;
import com.capstone.warranty_tracker.service.AdminService;
import com.capstone.warranty_tracker.service.TechnicianService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;

    @Mock
    private TechnicianService technicianService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    public void testGetStats_ReturnsStatsDto() throws Exception {
        // Arrange mock data
        AdminStatsDto mockStats = new AdminStatsDto(11, 11, 13, 1);
        when(adminService.getStats()).thenReturn(mockStats);

        // Act + Assert
        mockMvc.perform(get("/admin/stats"))  // 👈 make sure the route is correct
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTechnicians").value(11))
                .andExpect(jsonPath("$.pendingRequests").value(11))
                .andExpect(jsonPath("$.totalAppliances").value(13))
                .andExpect(jsonPath("$.completedRequests").value(1));
    }
    @Test
    void shouldReturnFirstRecentServiceRequestCorrectly() throws Exception {
        // Only 1 meaningful DTO (others are dummy)
        ServiceRequestAdminDto dto = new ServiceRequestAdminDto(
                101L,
                "Bosch BOS-DW321",
                null,
                "Mia Smith",
                "Ginny Miller",
                "REQUESTED",
                LocalDateTime.of(2025, 7, 1, 18, 0)
        );

        List<ServiceRequestAdminDto> mockList = List.of(
                dto,
                new ServiceRequestAdminDto(1132L, "dummy", "dummy", "dummy", "dummy", "dummy", LocalDateTime.now()),
                new ServiceRequestAdminDto(1232L, "dummy", "dummy", "dummy", "dummy", "dummy", LocalDateTime.now()),
                new ServiceRequestAdminDto(222L, "dummy", "dummy", "dummy", "dummy", "dummy", LocalDateTime.now()),
                new ServiceRequestAdminDto(201L, "dummy", "dummy", "dummy", "dummy", "dummy", LocalDateTime.now())
        );

        when(adminService.getRecentServiceRequest()).thenReturn(mockList);

        mockMvc.perform(get("/admin/recent-service-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.length()").value(5))
                .andExpect(jsonPath("$.body[0].id").value(101))
                .andExpect(jsonPath("$.body[0].applianceName").value("Bosch BOS-DW321"))
                .andExpect(jsonPath("$.body[0].serialNumber").doesNotExist()) // serialNumber is null
                .andExpect(jsonPath("$.body[0].homeownerName").value("Mia Smith"))
                .andExpect(jsonPath("$.body[0].technicianName").value("Ginny Miller"))
                .andExpect(jsonPath("$.body[0].status").value("REQUESTED"))
                .andExpect(jsonPath("$.body[0].createdAt[0]").value(2025))
                .andExpect(jsonPath("$.body[0].createdAt[1]").value(7))
                .andExpect(jsonPath("$.body[0].createdAt[2]").value(1))
                .andExpect(jsonPath("$.body[0].createdAt[3]").value(18))
                .andExpect(jsonPath("$.body[0].createdAt[4]").value(0));

        verify(adminService, times(1)).getRecentServiceRequest();
    }

    @Test
    void shouldReturnListOfTechnicians() throws Exception {
        TechnicianResponseDto dto = TechnicianResponseDto.builder()
                .id(6L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("9876543210")
                .specialization("Refrigerator Repair")
                .experience(5)
                .build();

        when(technicianService.getAllTechnicians()).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/all-technicians"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))  // response is a list
                .andExpect(jsonPath("$[0].id").value(6))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("9876543210"))
                .andExpect(jsonPath("$[0].specialization").value("Refrigerator Repair"))
                .andExpect(jsonPath("$[0].experience").value(5));

        verify(technicianService, times(1)).getAllTechnicians();
    }
}


