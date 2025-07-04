package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.ApplianceRequestDto;
import com.capstone.warranty_tracker.dto.ApplianceResponseDto;
import com.capstone.warranty_tracker.model.Appliance;
import com.capstone.warranty_tracker.model.Homeowner;
import com.capstone.warranty_tracker.repository.ApplianceRepository;
import com.capstone.warranty_tracker.repository.UserRepository;
import com.capstone.warranty_tracker.service.ApplianceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplianceServiceTest {

    @InjectMocks
    private ApplianceService applianceService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplianceRepository applianceRepository;

    @Mock
    private MultipartFile mockInvoiceFile;

    @BeforeEach
    void setup() {
        // No need for MockitoAnnotations.openMocks(this); when using @ExtendWith(MockitoExtension.class)
    }

    @Test
    void testGetHomeownerAppliances_success() {
        String email = "test@example.com";

        // Mock Homeowner
        Homeowner homeowner = new Homeowner();
        homeowner.setId(1L);
        homeowner.setEmail(email);

        // Mock Appliance
        Appliance appliance = new Appliance();
        appliance.setId(1L);
        appliance.setBrand("LG");
        appliance.setModelNumber("LG1234");
        appliance.setSerialNumber("SN12345");
        appliance.setPurchaseDate(LocalDate.of(2023, 1, 1));
        appliance.setWarrantyExpiryDate(LocalDate.of(2026, 1, 1));
        appliance.setInvoiceUrl("uploads/invoices/SN12345_invoice.pdf");
        appliance.setHomeowner(homeowner);

        // Mockito.when stubbing
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(homeowner));
        Mockito.when(applianceRepository.findByHomeowner_Id(homeowner.getId())).thenReturn(List.of(appliance));

        // Call service method
        List<ApplianceResponseDto> result = applianceService.getHomeownerAppliances(email);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ApplianceResponseDto dto = result.get(0);
        assertEquals("LG", dto.getBrand());
        assertEquals("LG1234", dto.getModelNumber());
        assertEquals("SN12345", dto.getSerialNumber());
        assertEquals(LocalDate.of(2023, 1, 1), dto.getPurchaseDate());
        assertEquals(LocalDate.of(2026, 1, 1), dto.getWarrantyExpiryDate());
        assertEquals("uploads/invoices/SN12345_invoice.pdf", dto.getInvoiceUrl());

        // Verify interactions
        Mockito.verify(userRepository).findByEmail(email);
        Mockito.verify(applianceRepository).findByHomeowner_Id(homeowner.getId());
    }


    @Test
    void testDeleteAppliance_Successful() {
        // Mock input
        String email = "user@example.com";
        Long applianceId = 1L;
        Long homeownerId = 100L;

        // Create mock homeowner
        Homeowner homeowner = new Homeowner();
        homeowner.setId(homeownerId);
        homeowner.setEmail(email);


        Appliance appliance = new Appliance();
        appliance.setId(applianceId);
        appliance.setSerialNumber("SN12345");
        appliance.setBrand("LG");
        appliance.setModelNumber("MOD123");
        appliance.setPurchaseDate(LocalDate.of(2023, 1, 1));
        appliance.setWarrantyExpiryDate(LocalDate.of(2025, 1, 1));
        appliance.setHomeowner(homeowner);

        // Mock repository behavior
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(homeowner));
        Mockito.when(applianceRepository.findById(applianceId)).thenReturn(Optional.of(appliance));

        // Call service
        assertDoesNotThrow(() -> applianceService.deleteAppliance(applianceId, email));

        // Verify repository interactions
        Mockito.verify(userRepository).findByEmail(email);
        Mockito.verify(applianceRepository).findById(applianceId);
        Mockito.verify(applianceRepository).delete(appliance);
    }


    @Test
    void testDeleteAppliance_ApplianceNotFound_ThrowsException() {
        // Mock input
        String email = "user@example.com";
        Long applianceId = 1L;


        Homeowner homeowner = new Homeowner();
        homeowner.setId(1L);
        homeowner.setEmail(email);


        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(homeowner));
        Mockito.when(applianceRepository.findById(applianceId)).thenReturn(Optional.empty());


        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            applianceService.deleteAppliance(applianceId, email);
        });


        assertEquals("Appliance not found with ID: " + applianceId, thrown.getMessage());


        Mockito.verify(userRepository).findByEmail(email);
        Mockito.verify(applianceRepository).findById(applianceId);
        Mockito.verify(applianceRepository, Mockito.never()).delete(Mockito.any());
    }

    @Test
    void testAddApplianceWithInvoice_Success() throws IOException {
        String email = "test@example.com";
        String fileName = "invoice.pdf";

        MultipartFile mockFile = mock(MultipartFile.class);
        Mockito.when(mockFile.getOriginalFilename()).thenReturn(fileName);
        Mockito.when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("Test Invoice".getBytes()));

        ApplianceRequestDto dto = new ApplianceRequestDto();
        dto.setBrand("Bosch");
        dto.setCategory("Oven");
        dto.setModelNumber("B123");

        String uniqueSerial = "SN" + System.currentTimeMillis();
        dto.setSerialNumber(uniqueSerial);
        dto.setPurchaseDate(LocalDate.of(2023, 1, 1));
        dto.setWarrantyExpiryDate(LocalDate.of(2025, 1, 1));

        Path invoicePath = Paths.get("uploads/invoices/" + uniqueSerial + "_" + fileName);
        Files.deleteIfExists(invoicePath);

        Homeowner homeowner = new Homeowner();
        homeowner.setId(2L);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(homeowner));
        Mockito.when(applianceRepository.existsBySerialNumber(uniqueSerial)).thenReturn(false);

        Appliance savedAppliance = new Appliance();
        savedAppliance.setId(100L);
        savedAppliance.setBrand(dto.getBrand());
        savedAppliance.setCategory(dto.getCategory());
        savedAppliance.setModelNumber(dto.getModelNumber());
        savedAppliance.setSerialNumber(dto.getSerialNumber());
        savedAppliance.setPurchaseDate(dto.getPurchaseDate());
        savedAppliance.setWarrantyExpiryDate(dto.getWarrantyExpiryDate());
        savedAppliance.setInvoiceUrl("uploads/invoices/" + uniqueSerial + "_" + fileName);
        savedAppliance.setHomeowner(homeowner);

        Mockito.when(applianceRepository.save(Mockito.any(Appliance.class))).thenReturn(savedAppliance);

        // ✅ Since method is void, just assert it doesn't throw
        assertDoesNotThrow(() -> applianceService.addApplianceWithInvoice(dto, mockFile, email));

        // ✅ Verify mocks
        Mockito.verify(userRepository).findByEmail(email);
        Mockito.verify(applianceRepository).existsBySerialNumber(uniqueSerial);
        Mockito.verify(applianceRepository).save(Mockito.any(Appliance.class));
    }


    @Test
    void testUpdateApplianceWithInvoice_Success() throws IOException {
        Long applianceId = 1L;
        String email = "user@example.com";
        String fileName = "updated_invoice.pdf";

        // Setup Homeowner
        Homeowner homeowner = new Homeowner();
        homeowner.setId(100L);
        homeowner.setEmail(email);

        // Existing appliance
        Appliance existingAppliance = new Appliance();
        existingAppliance.setId(applianceId);
        existingAppliance.setBrand("OldBrand");
        existingAppliance.setCategory("Microwave");
        existingAppliance.setModelNumber("OLD123");
        existingAppliance.setSerialNumber("OLDSN");
        existingAppliance.setPurchaseDate(LocalDate.of(2022, 1, 1));
        existingAppliance.setWarrantyExpiryDate(LocalDate.of(2024, 1, 1));
        existingAppliance.setHomeowner(homeowner);

        // Incoming update DTO
        ApplianceRequestDto dto = new ApplianceRequestDto();
        dto.setBrand("Samsung");
        dto.setCategory("Microwave");
        dto.setModelNumber("SM1234");
        dto.setSerialNumber("SN123456");
        dto.setPurchaseDate(LocalDate.of(2023, 1, 1));
        dto.setWarrantyExpiryDate(LocalDate.of(2026, 1, 1));

        // Mock MultipartFile
        MultipartFile invoiceFile = mock(MultipartFile.class);
        when(invoiceFile.getOriginalFilename()).thenReturn(fileName);
        when(invoiceFile.getInputStream()).thenReturn(new ByteArrayInputStream("Invoice data".getBytes()));
        when(invoiceFile.isEmpty()).thenReturn(false);

        // Mock repositories
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(homeowner));
        when(applianceRepository.findById(applianceId)).thenReturn(Optional.of(existingAppliance));
        when(applianceRepository.save(Mockito.any(Appliance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Call service
        ApplianceResponseDto response = applianceService.updateApplianceWithInvoice(applianceId, dto, invoiceFile, email);


        assertNotNull(response);
        assertEquals("Samsung", response.getBrand());
        assertEquals("SM1234", response.getModelNumber());
        assertEquals("SN123456", response.getSerialNumber());
        assertEquals(LocalDate.of(2023, 1, 1), response.getPurchaseDate());
        assertEquals(LocalDate.of(2026, 1, 1), response.getWarrantyExpiryDate());
        assertTrue(response.getInvoiceUrl().contains("SN123456_updated_invoice.pdf"));


        verify(userRepository).findByEmail(email);
        verify(applianceRepository).findById(applianceId);
        verify(applianceRepository).save(Mockito.any(Appliance.class));
    }



}
