package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.controller.HomeownerController;
import com.capstone.warranty_tracker.dto.ApplianceRequestDto;
import com.capstone.warranty_tracker.dto.ApplianceResponseDto;
import com.capstone.warranty_tracker.security.JwtUtil;
import com.capstone.warranty_tracker.service.ApplianceService;
import com.capstone.warranty_tracker.service.ServiceRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class HomeownerControllerTest {

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
    void testAddAppliance_WithInvoice_ReturnsSuccessMessage() throws Exception {
        String email = "test@example.com";

        ApplianceRequestDto request = new ApplianceRequestDto();
        request.setBrand("Samsung");
        request.setCategory("Washer");
        request.setModelNumber("WM123");
        request.setSerialNumber("SN123456");
        request.setPurchaseDate(LocalDate.of(2023, 1, 1));
        request.setWarrantyExpiryDate(LocalDate.of(2026, 1, 1));

        String requestJson = objectMapper.writeValueAsString(request);

        MockMultipartFile appliancePart = new MockMultipartFile(
                "appliance", "appliance.json", "application/json", requestJson.getBytes());

        MockMultipartFile invoicePart = new MockMultipartFile(
                "invoice", "invoice.pdf", "application/pdf", "Dummy PDF Content".getBytes());

        // ✅ Since method is void
        doNothing().when(applianceService).addApplianceWithInvoice(any(ApplianceRequestDto.class), any(MultipartFile.class), eq(email));

        mockMvc.perform(multipart("/homeowner/appliance")
                        .file(appliancePart)
                        .file(invoicePart)
                        .principal(() -> email)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Appliance registered successfully with invoice."));
    }

    @Test
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
    void testUpdateAppliance_WithInvoice_ReturnsSuccessMessage() throws Exception {
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

        // ✅ FIXED: Proper stubbing since the method returns a DTO
        when(applianceService.updateApplianceWithInvoice(eq(applianceId), any(ApplianceRequestDto.class), any(MultipartFile.class), eq(email)))
                .thenReturn(new ApplianceResponseDto());

        mockMvc.perform(multipart("/homeowner/edit/{id}", applianceId)
                        .file(appliancePart)
                        .file(invoicePart)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .principal(() -> email)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Appliance updated successfully"));
    }


    @Test
    @WithMockUser(username = "homeowner@example.com", roles = "HOMEOWNER")
    void testDeleteAppliance_Success() throws Exception {
        Long applianceId = 1L;
        String email = "homeowner@example.com";

        doNothing().when(applianceService).deleteAppliance(applianceId, email);

        mockMvc.perform(delete("/homeowner/delete/{id}", applianceId)
                        .principal(() -> email))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andExpect(header().doesNotExist("Content-Type"));

        verify(applianceService, times(1)).deleteAppliance(applianceId, email);
    }
}
