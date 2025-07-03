package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.*;
import com.capstone.warranty_tracker.model.ServiceStatus;
import com.capstone.warranty_tracker.service.AdminService;
import com.capstone.warranty_tracker.service.TechnicianService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;

    @Mock
    private TechnicianService technicianService;

    @Mock
    private ApplianceResponseDto applianceResponseDto;

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
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[0].applianceName").value("Bosch BOS-DW321"))
                .andExpect(jsonPath("$[0].serialNumber").doesNotExist()) // serialNumber is null
                .andExpect(jsonPath("$[0].homeownerName").value("Mia Smith"))
                .andExpect(jsonPath("$[0].technicianName").value("Ginny Miller"))
                .andExpect(jsonPath("$[0].status").value("REQUESTED"))
                .andExpect(jsonPath("$[0].createdAt[0]").value(2025))
                .andExpect(jsonPath("$[0].createdAt[1]").value(7))
                .andExpect(jsonPath("$[0].createdAt[2]").value(1))
                .andExpect(jsonPath("$[0].createdAt[3]").value(18))
                .andExpect(jsonPath("$[0].createdAt[4]").value(0));

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
//                .andExpect(jsonPath("$.length()").value(1))  // response is a list
                .andExpect(jsonPath("$[0].id").value(6))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("9876543210"))
                .andExpect(jsonPath("$[0].specialization").value("Refrigerator Repair"))
                .andExpect(jsonPath("$[0].experience").value(5));

        verify(technicianService, times(1)).getAllTechnicians();
    }
    @Test
    void shouldReturnListOfAvailableTechnicians() throws Exception {
        TechnicianResponseDto dto = TechnicianResponseDto.builder()
                .id(1001L)
                .firstName("Alyssa")
                .lastName("Turner")
                .email("alyssa.tools@example.com")
                .phoneNumber("9876543212")
                .specialization("Electrical Repair")
                .experience(5)
                .build();

        when(technicianService.getAvailableTechnicians()).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/available-technicians"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1001))
                .andExpect(jsonPath("$[0].firstName").value("Alyssa"))
                .andExpect(jsonPath("$[0].lastName").value("Turner"))
                .andExpect(jsonPath("$[0].email").value("alyssa.tools@example.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("9876543212"))
                .andExpect(jsonPath("$[0].specialization").value("Electrical Repair"))
                .andExpect(jsonPath("$[0].experience").value(5));

        verify(technicianService, times(1)).getAvailableTechnicians();
    }
    @Test
    void assignTechnicianToRequest_ReturnsOk_WhenAssignmentSuccessful() throws Exception {
        // Given
        Long technicianId = 1L;
        Long requestId = 100L;

        // When
        when(adminService.assignTechnicianToRequest(technicianId, requestId)).thenReturn(true);

        // Then
        mockMvc.perform(post("/admin/assign-technician")
                        .param("technicianId", technicianId.toString())
                        .param("requestId", requestId.toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string("Technician successfully assigned to service request."));
    }
    @Test
    void assignTechnicianToRequest_shouldReturnBadRequest_whenAssignmentFails() throws Exception {
        Long technicianId = 1L;
        Long requestId = 999L; // invalid ID

        when(adminService.assignTechnicianToRequest(technicianId, requestId)).thenReturn(false);

        mockMvc.perform(post("/admin/assign-technician")
                        .param("technicianId", technicianId.toString())
                        .param("requestId", requestId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Assignment failed. Check IDs or availability."));
    }

@Test
    void shouldReturnAllServiceRequests() throws Exception {
        ServiceRequestAdminDto dto = new ServiceRequestAdminDto(
                123L,
                "Samsung SM-R889",
                "101",
                "Kanjika Singh",
                "Ravi Sharma",
                "IN_PROGRESS",
                LocalDateTime.of(2025, 7, 1, 10, 0)
        );

        when(adminService.getAllServiceRequests()).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/all-service-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(123))
                .andExpect(jsonPath("$[0].applianceName").value("Samsung SM-R889"))
                .andExpect(jsonPath("$[0].serialNumber").value("101"))
                .andExpect(jsonPath("$[0].homeownerName").value("Kanjika Singh"))
                .andExpect(jsonPath("$[0].technicianName").value("Ravi Sharma"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].createdAt[0]").value(2025))
                .andExpect(jsonPath("$[0].createdAt[1]").value(7))
                .andExpect(jsonPath("$[0].createdAt[2]").value(1))
                .andExpect(jsonPath("$[0].createdAt[3]").value(10))
                .andExpect(jsonPath("$[0].createdAt[4]").value(0));

        verify(adminService, times(1)).getAllServiceRequests();
    }
    
    @Test
    void shouldReturnAllAppliances() throws Exception {
        ApplianceResponseDto dto = new ApplianceResponseDto(
                101L,
                "LG",
                "Refrigerator",
                "LG-X123",
                "SN123456",
                LocalDate.of(2024, 1, 10),
                "http://example.com/invoice.pdf",
                LocalDate.of(2026, 1, 10),
                "Kanjika Singh"
        );

        when(adminService.getAllAppliances()).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/all-appliances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[0].brand").value("LG"))
                .andExpect(jsonPath("$[0].category").value("Refrigerator"))
                .andExpect(jsonPath("$[0].modelNumber").value("LG-X123"))
                .andExpect(jsonPath("$[0].serialNumber").value("SN123456"))
                .andExpect(jsonPath("$[0].purchaseDate[0]").value(2024))
                .andExpect(jsonPath("$[0].purchaseDate[1]").value(1))
                .andExpect(jsonPath("$[0].purchaseDate[2]").value(10))
                .andExpect(jsonPath("$[0].invoiceUrl").value("http://example.com/invoice.pdf"))
                .andExpect(jsonPath("$[0].warrantyExpiryDate[0]").value(2026))
                .andExpect(jsonPath("$[0].warrantyExpiryDate[1]").value(1))
                .andExpect(jsonPath("$[0].warrantyExpiryDate[2]").value(10))
                .andExpect(jsonPath("$[0].homeownerName").value("Kanjika Singh"));

        verify(adminService, times(1)).getAllAppliances();
    }
    @Test
    void getAssignedRequests_shouldReturnJsonResponse() throws Exception {
        // Given
        ServiceRequestResponseDto dto = new ServiceRequestResponseDto(
                6001L,
                "Dishwasher not draining",
                LocalDateTime.of(2025, 6, 28, 10, 0),
                ServiceStatus.IN_PROGRESS,
                "Mia Smith",
                "Bosch BOS-DW321 (null)",
                LocalDateTime.of(2025, 6, 25, 10, 0)
        );

        when(technicianService.getAssignedRequestsForTechnicianByID(1L))
                .thenReturn(List.of(dto));

        // When & Then
        mockMvc.perform(get("/admin/technician-assigned-requests")
                        .param("technicianId", "1"))
//                .andExpect(status().isOk()) // ✅ Always check status first
                .andExpect(jsonPath("$[0].id").value(6001))
                .andExpect(jsonPath("$[0].issueDescription").value("Dishwasher not draining"))
//                .andExpect(jsonPath("$[0].preferredSlot").value("2025-06-28T10:00:00"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].homeownerName").value("Mia Smith"))
                .andExpect(jsonPath("$[0].applianceInfo").value("Bosch BOS-DW321 (null)"));
//                .andExpect(jsonPath("$[0].createdAt").value("2025-06-25T10:00:00"));
    }
    @Test
    void getInProgressRequests_shouldReturnJsonResponse() throws Exception {
        ServiceRequestResponseDto dto = new ServiceRequestResponseDto(
                6002L,
                "Washing machine leaking",
                LocalDateTime.of(2025, 7, 1, 14, 0),
                ServiceStatus.IN_PROGRESS,
                "Amit Verma",
                "LG WMX123 (SN2001)",
                LocalDateTime.of(2025, 6, 29, 9, 0)
        );

        when(technicianService.getInProgressRequestsForTechnician("ravi@example.com"))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/technician/in-progress")
                        .param("technicianEmail", "ravi@example.com"))
                .andExpect(jsonPath("$[0].id").value(6002))
                .andExpect(jsonPath("$[0].issueDescription").value("Washing machine leaking"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].homeownerName").value("Amit Verma"))
                .andExpect(jsonPath("$[0].applianceInfo").value("LG WMX123 (SN2001)"));
    }
    @Test
    void getCompletedRequests_shouldReturnJsonResponse() throws Exception {
        ServiceRequestResponseDto dto = new ServiceRequestResponseDto(
                6003L,
                "AC not cooling",
                LocalDateTime.of(2025, 6, 27, 16, 30),
                ServiceStatus.COMPLETED,
                "Priya Sharma",
                "Samsung ACX900 (SN54321)",
                LocalDateTime.of(2025, 6, 24, 8, 45)
        );

        when(technicianService.getCompletedRequestsForTechnician("ravi@example.com"))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/technician/completed")
                        .param("technicianEmail", "ravi@example.com"))
                .andExpect(jsonPath("$[0].id").value(6003))
                .andExpect(jsonPath("$[0].issueDescription").value("AC not cooling"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].homeownerName").value("Priya Sharma"))
                .andExpect(jsonPath("$[0].applianceInfo").value("Samsung ACX900 (SN54321)"));
    }

}


