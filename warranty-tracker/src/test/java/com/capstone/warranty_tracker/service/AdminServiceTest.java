package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.AdminStatsDto;
import com.capstone.warranty_tracker.dto.ServiceRequestAdminDto;
import com.capstone.warranty_tracker.model.*;
import com.capstone.warranty_tracker.repository.ApplianceRepository;
import com.capstone.warranty_tracker.repository.ServiceRequestRepository;
import com.capstone.warranty_tracker.repository.TechnicianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class AdminServiceTest {

    @Mock
    private ApplianceRepository applianceRepository;

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private TechnicianRepository technicianRepository;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // ✅ Initialize mocks and inject them
    }

    @Test
    public void testGetStats_ReturnsCorrectCounts() {
        // Arrange
        when(applianceRepository.count()).thenReturn(13L);
        when(technicianRepository.count()).thenReturn(11L);
        when(serviceRequestRepository.countByStatusNot(ServiceStatus.COMPLETED)).thenReturn(11L);
        when(serviceRequestRepository.countByStatus(ServiceStatus.COMPLETED)).thenReturn(1L);

        // Act
        AdminStatsDto result = adminService.getStats();

        // Assert
        assertEquals(11, result.getTotalTechnicians());
        assertEquals(11, result.getPendingRequests());
        assertEquals(13, result.getTotalAppliances());
        assertEquals(1, result.getCompletedRequests());
    }

        @Test
        void shouldReturnMappedServiceRequestAdminDtoList() {
            // Mock entities
            Appliance appliance = new Appliance();
            appliance.setBrand("Bosch");
            appliance.setModelNumber("BOS-DW321");
            appliance.setSerialNumber(null);

            Homeowner homeowner = new Homeowner();
            homeowner.setFirstName("Mia");
            homeowner.setLastName("Smith");

            Technician technician = new Technician();
            technician.setFirstName("Ginny");
            technician.setLastName("Miller");

            ServiceRequest request = new ServiceRequest();
            request.setId(101L);
            request.setAppliance(appliance);
            request.setHomeowner(homeowner);
            request.setTechnician(technician);
            request.setStatus(ServiceStatus.REQUESTED);
            request.setCreatedAt(LocalDateTime.of(2025, 7, 1, 18, 0));

            when(serviceRequestRepository.findTop5ByOrderByCreatedAtDesc())
                    .thenReturn(List.of(request));

            // Run
            List<ServiceRequestAdminDto> result = adminService.getRecentServiceRequest();

            // Assert
            assertEquals(1, result.size());
            ServiceRequestAdminDto dto = result.get(0);
            assertEquals(101L, dto.getId());
            assertEquals("Bosch BOS-DW321", dto.getApplianceName());
            assertNull(dto.getSerialNumber());
            assertEquals("Mia Smith", dto.getHomeownerName());
            assertEquals("Ginny Miller", dto.getTechnicianName());
            assertEquals("REQUESTED", dto.getStatus());
            assertEquals(LocalDateTime.of(2025, 7, 1, 18, 0), dto.getCreatedAt());

            verify(serviceRequestRepository, times(1)).findTop5ByOrderByCreatedAtDesc();
        }


}
