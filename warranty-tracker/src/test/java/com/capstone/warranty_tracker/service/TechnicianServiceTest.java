package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.TechnicianResponseDto;
import com.capstone.warranty_tracker.model.Technician;
import com.capstone.warranty_tracker.repository.TechnicianRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnicianServiceTest {

    @Mock
    private TechnicianRepository technicianRepository;

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

}
