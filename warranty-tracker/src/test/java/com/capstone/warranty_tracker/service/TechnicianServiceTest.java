package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.CompletionFormDto;
import com.capstone.warranty_tracker.dto.TechnicianResponseDto;
import com.capstone.warranty_tracker.dto.UpdateRequestStatusDto;
import com.capstone.warranty_tracker.model.*;
import com.capstone.warranty_tracker.repository.CompletionFormRepository;
import com.capstone.warranty_tracker.repository.ServiceRequestRepository;
import com.capstone.warranty_tracker.repository.TechnicianRepository;
import com.capstone.warranty_tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnicianServiceTest {

    @Mock
    private TechnicianRepository technicianRepository;
    @Mock private ServiceRequestRepository serviceRequestRepository;
    @Mock private UserRepository userRepository;
    @Mock private CompletionFormRepository completionFormRepository;
    @Mock private ServiceRequestService serviceRequestService;

    @InjectMocks
    private TechnicianService technicianService;

    @Test
    void shouldMapTechnicianEntityToDto() {
        // Arrange
        Technician tech = new Technician();
        tech.setId(6L);
        tech.setFirstName("John");
        tech.setLastName("Doe");
        tech.setEmail("john.doe@example.com");
        tech.setPhoneNumber("9876543210");
        tech.setSpecialization("Refrigerator Repair");
        tech.setExperience(5);

        when(technicianRepository.findAll()).thenReturn(List.of(tech));

        // Act
        List<TechnicianResponseDto> result = technicianService.getAllTechnicians();

        // Assert
        assertEquals(1, result.size());
        TechnicianResponseDto dto = result.get(0);
        assertEquals(6L, dto.getId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john.doe@example.com", dto.getEmail());
        assertEquals("9876543210", dto.getPhoneNumber());
        assertEquals("Refrigerator Repair", dto.getSpecialization());
        assertEquals(5, dto.getExperience());

        // Verify interaction
        verify(technicianRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnAvailableTechniciansMappedToDto() {
        // Arrange
        Technician tech = new Technician();
        tech.setId(10L);
        tech.setFirstName("Alyssa");
        tech.setLastName("Turner");
        tech.setEmail("alyssa.tools@example.com");
        tech.setPhoneNumber("9876543212");
        tech.setSpecialization("Electrical Repair");
        tech.setExperience(5);

        when(technicianRepository.findTechniciansWithNoServiceRequests()).thenReturn(List.of(tech));

        // Act
        List<TechnicianResponseDto> result = technicianService.getAvailableTechnicians();

        // Assert
        assertEquals(1, result.size());
        TechnicianResponseDto dto = result.get(0);
        assertEquals(10L, dto.getId());
        assertEquals("Alyssa", dto.getFirstName());
        assertEquals("Turner", dto.getLastName());
        assertEquals("alyssa.tools@example.com", dto.getEmail());
        assertEquals("9876543212", dto.getPhoneNumber());
        assertEquals("Electrical Repair", dto.getSpecialization());
        assertEquals(5, dto.getExperience());

        verify(technicianRepository, times(1)).findTechniciansWithNoServiceRequests();
    }

    @Test
    void shouldReturnAssignedRequestsForTechnician() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setIssueDescription("Broken fridge");
        sr.setPreferredSlot(LocalDateTime.now());
        sr.setStatus(ServiceStatus.ASSIGNED);

        Homeowner owner = new Homeowner();
        owner.setFirstName("Alice");
        owner.setLastName("Smith");
        sr.setHomeowner(owner);

        Appliance appliance = new Appliance();
        appliance.setBrand("Samsung");
        appliance.setModelNumber("X123");
        appliance.setSerialNumber("SN123");
        sr.setAppliance(appliance);

        sr.setCreatedAt(LocalDateTime.now());

        when(serviceRequestRepository.findAssignedRequestsByTechnicianEmail("tech@example.com"))
                .thenReturn(List.of(sr));

        var result = technicianService.getAssignedRequestsForTechnician("tech@example.com");
        assertEquals(1, result.size());
        assertEquals("Broken fridge", result.get(0).getIssueDescription());
    }

    @Test
    void shouldUpdateRequestStatus() {
        Technician tech = new Technician();
        tech.setId(1L);
        tech.setEmail("tech@example.com");

        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setTechnician(tech);

        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));

        UpdateRequestStatusDto dto = new UpdateRequestStatusDto();
        dto.setRequestId(1L);
        dto.setStatus(ServiceStatus.IN_PROGRESS);

        technicianService.updateRequestStatus("tech@example.com", dto);

        assertEquals(ServiceStatus.IN_PROGRESS, sr.getStatus());
        verify(serviceRequestRepository).save(sr);
    }

    @Test
    void shouldReturnInProgressRequestsForTechnician() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(2L);
        sr.setStatus(ServiceStatus.IN_PROGRESS);
        sr.setIssueDescription("Washing machine issue");
        sr.setPreferredSlot(LocalDateTime.now());
        sr.setCreatedAt(LocalDateTime.now());

        Homeowner ho = new Homeowner();
        ho.setFirstName("Bob");
        ho.setLastName("Marley");
        sr.setHomeowner(ho);

        Appliance appliance = new Appliance();
        appliance.setBrand("LG");
        appliance.setModelNumber("WM2020");
        appliance.setSerialNumber("SN456");
        sr.setAppliance(appliance);

        when(serviceRequestRepository.findAssignedRequestsByTechnicianEmail("tech@example.com"))
                .thenReturn(List.of(sr));

        var result = technicianService.getInProgressRequestsForTechnician("tech@example.com");
        assertEquals(1, result.size());
        assertEquals(ServiceStatus.IN_PROGRESS, result.get(0).getStatus());
    }

    @Test
    void shouldReturnCompletedRequestsForTechnician() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(3L);
        sr.setStatus(ServiceStatus.COMPLETED);
        sr.setIssueDescription("Oven repair");
        sr.setPreferredSlot(LocalDateTime.now());
        sr.setCreatedAt(LocalDateTime.now());

        Homeowner ho = new Homeowner();
        ho.setFirstName("Sam");
        ho.setLastName("Rogers");
        sr.setHomeowner(ho);

        Appliance appliance = new Appliance();
        appliance.setBrand("Whirlpool");
        appliance.setModelNumber("OV333");
        appliance.setSerialNumber("SN789");
        sr.setAppliance(appliance);

        when(serviceRequestRepository.findAssignedRequestsByTechnicianEmail("tech@example.com"))
                .thenReturn(List.of(sr));

        var result = technicianService.getCompletedRequestsForTechnician("tech@example.com");
        assertEquals(1, result.size());
        assertEquals(ServiceStatus.COMPLETED, result.get(0).getStatus());
    }

    @Test
    void shouldReturnTechnicianProfile() {
        Technician tech = new Technician();
        tech.setId(5L);
        tech.setFirstName("Jane");
        tech.setLastName("Doe");
        tech.setEmail("jane@example.com");
        tech.setPhoneNumber("1234567890");
        tech.setSpecialization("Microwave Repair");
        tech.setExperience(4);

        when(technicianRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(tech));

        TechnicianResponseDto result = technicianService.getTechnicianProfile("jane@example.com");

        assertEquals("Jane", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("jane@example.com", result.getEmail());
    }

    @Test
    void shouldReturnTechnicianStats() {
        ServiceRequest assigned = new ServiceRequest();
        assigned.setStatus(ServiceStatus.ASSIGNED);
        ServiceRequest inProgress = new ServiceRequest();
        inProgress.setStatus(ServiceStatus.IN_PROGRESS);
        ServiceRequest completed = new ServiceRequest();
        completed.setStatus(ServiceStatus.COMPLETED);

        when(serviceRequestRepository.findAssignedRequestsByTechnicianEmail("tech@example.com"))
                .thenReturn(List.of(assigned, inProgress, completed));

        var stats = technicianService.getTechnicianStats("tech@example.com");

        assertEquals(1, stats.getAssignedCount());
        assertEquals(1, stats.getInProgressCount());
        assertEquals(1, stats.getCompletedCount());
    }

    @Test
    void shouldSaveCompletionForm() {
        Technician tech = new Technician();
        tech.setId(1L);
        tech.setEmail("tech@example.com");

        ServiceRequest sr = new ServiceRequest();
        sr.setId(100L);
        sr.setTechnician(tech);

        when(userRepository.findByEmail("tech@example.com")).thenReturn(Optional.of(tech));
        when(serviceRequestRepository.findById(100L)).thenReturn(Optional.of(sr));

        CompletionFormDto dto = new CompletionFormDto();
        dto.setCompletionDate("2024-01-01");
        dto.setCompletionTime("12:00");
        dto.setTechnicianNotes("All done");
        dto.setConfirmed(true);

        technicianService.saveCompletionForm(100L, dto, "tech@example.com");

        verify(completionFormRepository, times(1)).save(any(CompletionForm.class));
    }

    @Test
    void shouldReturnCompletionForm() {
        Technician tech = new Technician();
        tech.setId(1L);
        tech.setEmail("tech@example.com");

        ServiceRequest sr = new ServiceRequest();
        sr.setId(200L);

        CompletionForm form = new CompletionForm();
        form.setId(10L);
        form.setServiceRequest(sr);
        form.setTechnician(tech);
        form.setCompletionDate("2024-01-01");
        form.setCompletionTime("13:00");
        form.setTechnicianNotes("Fixed");
        form.setConfirmed(true);

        when(userRepository.findByEmail("tech@example.com")).thenReturn(Optional.of(tech));
        when(completionFormRepository.findByServiceRequest_Id(200L)).thenReturn(form);

        var result = technicianService.getCompletionForm(200L, "tech@example.com");

        assertEquals(10L, result.getId());
        assertEquals("2024-01-01", result.getCompletionDate());
        assertEquals("13:00", result.getCompletionTime());
        assertTrue(result.isConfirmed());
        assertEquals(200L, result.getServiceRequestId());
        assertEquals(1L, result.getTechnicianId());
    }


}
