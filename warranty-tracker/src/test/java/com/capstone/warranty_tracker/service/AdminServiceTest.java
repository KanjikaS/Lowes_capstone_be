package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.AdminStatsDto;
import com.capstone.warranty_tracker.dto.ApplianceResponseDto;
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

import java.time.LocalDate;
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

    @Test
    void shouldMapServiceRequestToAdminDto() {
        // Mock technician
        Technician technician = new Technician();
        technician.setFirstName("Ravi");
        technician.setLastName("Sharma");

        // Mock appliance
        Appliance appliance = new Appliance();
        appliance.setBrand("Samsung");
        appliance.setModelNumber("SM-R889");
        appliance.setSerialNumber("101");

        // Mock homeowner
        Homeowner homeowner = new Homeowner();
        homeowner.setFirstName("Kanjika");
        homeowner.setLastName("Singh");

        // Mock service request
        ServiceRequest sr = new ServiceRequest();
        sr.setId(123L);
        sr.setAppliance(appliance);
        sr.setHomeowner(homeowner);
        sr.setTechnician(technician);
        sr.setStatus(ServiceStatus.IN_PROGRESS);
        sr.setCreatedAt(LocalDateTime.of(2025, 7, 1, 10, 0));

        when(serviceRequestRepository.findAll()).thenReturn(List.of(sr));

        List<ServiceRequestAdminDto> result = adminService.getAllServiceRequests();

        assertEquals(1, result.size());
        ServiceRequestAdminDto dto = result.get(0);

        assertEquals(123L, dto.getId());
        assertEquals("Samsung SM-R889", dto.getApplianceName());
        assertEquals("101", dto.getSerialNumber());
        assertEquals("Kanjika Singh", dto.getHomeownerName());
        assertEquals("Ravi Sharma", dto.getTechnicianName());
        assertEquals("IN_PROGRESS", dto.getStatus());
        assertEquals(LocalDateTime.of(2025, 7, 1, 10, 0), dto.getCreatedAt());

        verify(serviceRequestRepository, times(1)).findAll();
    }
    @Test
    void shouldMapApplianceToDto() {
        Homeowner homeowner = new Homeowner();
        homeowner.setFirstName("Kanjika");
        homeowner.setLastName("Singh");

        Appliance appliance = new Appliance();
        appliance.setId(101L);
        appliance.setBrand("LG");
        appliance.setCategory("Refrigerator");
        appliance.setModelNumber("LG-X123");
        appliance.setSerialNumber("SN123456");
        appliance.setPurchaseDate(LocalDate.of(2024, 1, 10));
        appliance.setInvoiceUrl("http://example.com/invoice.pdf");
        appliance.setWarrantyExpiryDate(LocalDate.of(2026, 1, 10));
        appliance.setHomeowner(homeowner);

        when(applianceRepository.findAll()).thenReturn(List.of(appliance));

        List<ApplianceResponseDto> result = adminService.getAllAppliances();

        assertEquals(1, result.size());
        ApplianceResponseDto dto = result.get(0);

        assertEquals(101L, dto.getId());
        assertEquals("LG", dto.getBrand());
        assertEquals("Refrigerator", dto.getCategory());
        assertEquals("LG-X123", dto.getModelNumber());
        assertEquals("SN123456", dto.getSerialNumber());
        assertEquals(LocalDate.of(2024, 1, 10), dto.getPurchaseDate());
        assertEquals("http://example.com/invoice.pdf", dto.getInvoiceUrl());
        assertEquals(LocalDate.of(2026, 1, 10), dto.getWarrantyExpiryDate());
        assertEquals("Kanjika Singh", dto.getHomeownerName());

        verify(applianceRepository, times(1)).findAll();
    }



}
