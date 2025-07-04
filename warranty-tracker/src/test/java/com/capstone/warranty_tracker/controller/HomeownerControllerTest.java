package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.controller.HomeownerController;
import com.capstone.warranty_tracker.dto.ApplianceRequestDto;
import com.capstone.warranty_tracker.dto.ApplianceResponseDto;
import com.capstone.warranty_tracker.dto.ServiceRequestDto;
import com.capstone.warranty_tracker.dto.ServiceRequestResponseDto;
import com.capstone.warranty_tracker.security.JwtUtil;
import com.capstone.warranty_tracker.service.ApplianceService;
import com.capstone.warranty_tracker.service.ServiceRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
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

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(IllegalArgumentException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public String handleIllegalArgumentException(IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    @BeforeEach
    void setUp() {
        // Set up ObjectMapper with JavaTimeModule for LocalDateTime serialization
        objectMapper.registerModule(new JavaTimeModule());
    }

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

    @Test
    void testCreateServiceRequest() throws Exception {
        // Step 1: Create test data
        ServiceRequestDto requestDto = new ServiceRequestDto();
        requestDto.setSerialNumber("SN001");
        requestDto.setIssueDescription("Washing machine not working");
        requestDto.setPreferredSlot(LocalDateTime.now().plusDays(1));

        ServiceRequestResponseDto responseDto = new ServiceRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setIssueDescription("Washing machine not working");
        responseDto.setHomeownerName("John Doe");
        responseDto.setApplianceInfo("Samsung WF45R6100AC (SN: SN001)");

        Principal principal = () -> "john@test.com";

        // Step 2: Set up mock behavior
        when(serviceRequestService.createRequest(any(ServiceRequestDto.class), eq("john@test.com")))
                .thenReturn(responseDto);

        // Step 3: Make the HTTP request and check response
        mockMvc.perform(post("/homeowner/service-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.issueDescription").value("Washing machine not working"))
                .andExpect(jsonPath("$.homeownerName").value("John Doe"));
    }

    @Test
    void testGetAllServiceRequests() throws Exception {
        // Step 1: Create test data
        ServiceRequestResponseDto responseDto = new ServiceRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setIssueDescription("Washing machine not working");
        responseDto.setHomeownerName("John Doe");

        List<ServiceRequestResponseDto> requests = Arrays.asList(responseDto);
        Principal principal = () -> "john@test.com";

        // Step 2: Set up mock behavior
        when(serviceRequestService.getHomeownerRequests("john@test.com")).thenReturn(requests);

        // Step 3: Make the HTTP request and check response
        mockMvc.perform(get("/homeowner/service-requests")
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].issueDescription").value("Washing machine not working"));
    }

    @Test
    void testGetServiceRequestById() throws Exception {
        // Step 1: Create test data
        ServiceRequestResponseDto responseDto = new ServiceRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setIssueDescription("Washing machine not working");
        responseDto.setApplianceInfo("Samsung WF45R6100AC (SN: SN001)");

        Principal principal = () -> "john@test.com";

        // Step 2: Set up mock behavior
        when(serviceRequestService.getRequestById(1L, "john@test.com"))
                .thenReturn(responseDto);

        // Step 3: Make the HTTP request and check response
        mockMvc.perform(get("/homeowner/service-request/1")
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.applianceInfo").value("Samsung WF45R6100AC (SN: SN001)"));
    }

    @Test
    void testUpdateServiceRequest() throws Exception {
        // Step 1: Create test data
        ServiceRequestDto requestDto = new ServiceRequestDto();
        requestDto.setSerialNumber("SN001");
        requestDto.setIssueDescription("Updated issue description");
        requestDto.setPreferredSlot(LocalDateTime.now().plusDays(2));

        ServiceRequestResponseDto responseDto = new ServiceRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setIssueDescription("Updated issue description");

        Principal principal = () -> "john@test.com";

        // Step 2: Set up mock behavior
        when(serviceRequestService.updateRequest(eq(1L), any(ServiceRequestDto.class), eq("john@test.com")))
                .thenReturn(responseDto);

        // Step 3: Make the HTTP request and check response
        mockMvc.perform(put("/homeowner/service-request/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.issueDescription").value("Updated issue description"));
    }

    @Test
    void testCancelServiceRequest() throws Exception {
        // Step 1: Create test data
        Principal principal = () -> "john@test.com";

        // Step 2: Set up mock behavior (no return value needed for void method)

        // Step 3: Make the HTTP request and check response
        mockMvc.perform(delete("/homeowner/service-request/1")
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(content().string("Service request cancelled."));
    }

    @Test
    void testGetAllServiceRequestsEmpty() throws Exception {
        // Step 1: Create test data
        Principal principal = () -> "john@test.com";

        // Step 2: Set up mock behavior to return empty list
        when(serviceRequestService.getHomeownerRequests("john@test.com")).thenReturn(Arrays.asList());

        // Step 3: Make the HTTP request and check response
        mockMvc.perform(get("/homeowner/service-requests")
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
