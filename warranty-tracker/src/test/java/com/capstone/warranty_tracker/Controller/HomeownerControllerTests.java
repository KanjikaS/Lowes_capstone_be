package com.capstone.warranty_tracker.Controller;

import com.capstone.warranty_tracker.controller.HomeownerController;
import com.capstone.warranty_tracker.dto.ApplianceRequestDto;
import com.capstone.warranty_tracker.dto.ApplianceResponseDto;
import com.capstone.warranty_tracker.security.JwtUtil;
import com.capstone.warranty_tracker.security.SecurityConfig;
import com.capstone.warranty_tracker.service.ApplianceService;
import com.capstone.warranty_tracker.service.ServiceRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
//@Import(SecurityConfig.class) // if you still need config
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
//@WebMvcTest(controllers = HomeownerController.class)
class HomeownerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplianceService applianceService;

    @MockBean
    private ServiceRequestService serviceRequestService;

    @MockBean
    private JwtUtil jwtUtil;


    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAddAppliance_WithInvoice_ReturnsDto() throws Exception {
        String email = "test@example.com";
        String serialNumber = "SN123456";

        ApplianceRequestDto request = new ApplianceRequestDto();
        request.setBrand("Samsung");
        request.setCategory("Washer");
        request.setModelNumber("WM123");
        request.setSerialNumber(serialNumber);
        request.setPurchaseDate(LocalDate.of(2023, 1, 1));
        request.setWarrantyExpiryDate(LocalDate.of(2026, 1, 1));

        ApplianceResponseDto response = new ApplianceResponseDto();
        response.setId(1L);
        response.setBrand("Samsung");
        response.setSerialNumber(serialNumber);
        response.setInvoiceUrl("uploads/invoices/SN123456_invoice.pdf");
        response.setPurchaseDate(LocalDate.of(2023, 1, 1));
        response.setWarrantyExpiryDate(LocalDate.of(2026, 1, 1));

        String requestJson = objectMapper.writeValueAsString(request);

        MockMultipartFile appliancePart = new MockMultipartFile(
                "appliance", "appliance.json", "application/json", requestJson.getBytes());

        MockMultipartFile invoicePart = new MockMultipartFile(
                "invoice", "invoice.pdf", "application/pdf", "Dummy PDF Content".getBytes());

        when(applianceService.addApplianceWithInvoice(any(ApplianceRequestDto.class), any(MultipartFile.class), eq(email)))
                .thenReturn(response);

        mockMvc.perform(multipart("/homeowner/appliance")
                        .file(appliancePart)
                        .file(invoicePart)
                        .principal(() -> email)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand").value("Samsung"))
                .andExpect(jsonPath("$.serialNumber").value(serialNumber))
                .andExpect(jsonPath("$.invoiceUrl").value("uploads/invoices/SN123456_invoice.pdf"));
    }

    @Test
   // @WithMockUser(username = "homeowner@example.com", roles = "HOMEOWNER")
    void testGetAllAppliances() throws Exception {
        List<ApplianceResponseDto> appliances = List.of(new ApplianceResponseDto());
        when(applianceService.getHomeownerAppliances("homeowner@example.com")).thenReturn(appliances);

        mockMvc.perform(get("/homeowner/appliances")
                        .principal(() -> "homeowner@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "homeowner@example.com", roles = "HOMEOWNER")
    void testUpdateAppliance_WithInvoice() throws Exception {
        Long applianceId = 1L;
        String email = "homeowner@example.com";

        ApplianceRequestDto requestDto = new ApplianceRequestDto();
        requestDto.setBrand("LG");
        requestDto.setCategory("Fridge");
        requestDto.setModelNumber("FR123");
        requestDto.setSerialNumber("SN999");
        requestDto.setPurchaseDate(LocalDate.of(2023, 2, 2));
        requestDto.setWarrantyExpiryDate(LocalDate.of(2026, 2, 2));

        String applianceJson = objectMapper.writeValueAsString(requestDto);

        MockMultipartFile appliancePart = new MockMultipartFile(
                "appliance", "appliance.json", "application/json", applianceJson.getBytes());

        MockMultipartFile invoicePart = new MockMultipartFile(
                "invoice", "invoice.pdf", "application/pdf", "dummy pdf".getBytes());

        // Since controller discards the return value, we just mock it to return any DTO
        when(applianceService.updateApplianceWithInvoice(
                eq(applianceId), any(ApplianceRequestDto.class), any(MultipartFile.class), eq(email)))
                .thenReturn(new ApplianceResponseDto()); // value discarded anyway

        mockMvc.perform(multipart("/homeowner/edit/{id}", applianceId)
                        .file(appliancePart)
                        .file(invoicePart)
                        .with(request -> { request.setMethod("PUT"); return request; }) // Force PUT
                        .principal(() -> email)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Appliance updated successfully")); // ✅ match plain text
    }

    @Test
    @WithMockUser(username = "homeowner@example.com", roles = "HOMEOWNER")
    void testDeleteAppliance_Success() throws Exception {
        Long applianceId = 1L;
        String email = "homeowner@example.com";

        // Optional but good practice for void methods
        doNothing().when(applianceService).deleteAppliance(applianceId, email);

        // Act & Assert
        mockMvc.perform(delete("/homeowner/delete/{id}", applianceId)
                        .principal(() -> email))
                .andExpect(status().isNoContent()) // ✅ Assert: 204 No Content
                .andExpect(content().string(""))   // ✅ Assert: Response body is empty
                .andExpect(header().doesNotExist("Content-Type")); // ✅ No content type in 204

        // Verify service was called correctly
        verify(applianceService, times(1)).deleteAppliance(applianceId, email);
    }






}
