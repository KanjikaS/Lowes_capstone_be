package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.ServiceRequestDto;
import com.capstone.warranty_tracker.dto.ServiceRequestResponseDto;
import com.capstone.warranty_tracker.model.*;
import com.capstone.warranty_tracker.repository.ApplianceRepository;
import com.capstone.warranty_tracker.repository.ServiceRequestRepository;
import com.capstone.warranty_tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplianceRepository applianceRepository;

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @InjectMocks
    private ServiceRequestService serviceRequestService;

    @Test
    void testCreateServiceRequestSuccess() {
        Homeowner homeowner = new Homeowner();
        homeowner.setId(1L);
        homeowner.setEmail("john@test.com");
        homeowner.setFirstName("John");
        homeowner.setLastName("Doe");

        Appliance appliance = new Appliance();
        appliance.setId(1L);
        appliance.setSerialNumber("SN001");
        appliance.setBrand("Samsung");
        appliance.setModelNumber("WF45R6100AC");
        appliance.setHomeowner(homeowner);

        ServiceRequestDto requestDto = new ServiceRequestDto();
        requestDto.setSerialNumber("SN001");
        requestDto.setIssueDescription("Washing machine not working");
        requestDto.setPreferredSlot(LocalDateTime.now().plusDays(1));

        ServiceRequest savedRequest = new ServiceRequest();
        savedRequest.setId(1L);
        savedRequest.setIssueDescription("Washing machine not working");
        savedRequest.setStatus(ServiceStatus.REQUESTED);
        savedRequest.setHomeowner(homeowner);
        savedRequest.setAppliance(appliance);

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(homeowner));
        when(applianceRepository.findBySerialNumber("SN001")).thenReturn(Optional.of(appliance));
        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenReturn(savedRequest);

        ServiceRequestResponseDto result = serviceRequestService.createRequest(requestDto, "john@test.com");

        assertNotNull(result);
        assertEquals("Washing machine not working", result.getIssueDescription());
        assertEquals(ServiceStatus.REQUESTED, result.getStatus());
    }

    @Test
    void testCreateServiceRequestHomeownerNotFound() {
        // Create test data
        ServiceRequestDto requestDto = new ServiceRequestDto();
        requestDto.setSerialNumber("SN001");
        requestDto.setIssueDescription("Washing machine not working");

        // Set up mock to return empty (homeowner not found)
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());

        // Call the service method and expect exception
        assertThrows(UsernameNotFoundException.class, () ->
                serviceRequestService.createRequest(requestDto, "john@test.com"));
    }

    @Test
    void testCreateServiceRequestApplianceNotFound() {
        // Create test data
        Homeowner homeowner = new Homeowner();
        homeowner.setId(1L);
        homeowner.setEmail("john@test.com");

        ServiceRequestDto requestDto = new ServiceRequestDto();
        requestDto.setSerialNumber("SN999");
        requestDto.setIssueDescription("Washing machine not working");

        // Set up mocks
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(homeowner));
        when(applianceRepository.findBySerialNumber("SN999")).thenReturn(Optional.empty());

        // Call the service method and expect exception
        assertThrows(IllegalArgumentException.class, () ->
                serviceRequestService.createRequest(requestDto, "john@test.com"));
    }

    @Test
    void testCreateServiceRequestApplianceNotOwnedByHomeowner() {
        // Step 1: Create test data
        Homeowner homeowner1 = new Homeowner();
        homeowner1.setId(1L);
        homeowner1.setEmail("john@test.com");

        Homeowner homeowner2 = new Homeowner();
        homeowner2.setId(2L);
        homeowner2.setEmail("jane@test.com");

        Appliance appliance = new Appliance();
        appliance.setId(1L);
        appliance.setSerialNumber("SN001");
        appliance.setHomeowner(homeowner2);

        ServiceRequestDto requestDto = new ServiceRequestDto();
        requestDto.setSerialNumber("SN001");
        requestDto.setIssueDescription("Washing machine not working");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(homeowner1));
        when(applianceRepository.findBySerialNumber("SN001")).thenReturn(Optional.of(appliance));

        // Step 3: Call the service method and expect exception
        assertThrows(AccessDeniedException.class, () ->
                serviceRequestService.createRequest(requestDto, "john@test.com"));
    }

    @Test
    void testGetHomeownerRequests() {
        Homeowner homeowner = new Homeowner();
        homeowner.setId(1L);
        homeowner.setEmail("john@test.com");
        homeowner.setFirstName("John");
        homeowner.setLastName("Doe");

        Appliance appliance = new Appliance();
        appliance.setId(1L);
        appliance.setSerialNumber("SN001");
        appliance.setBrand("Samsung");

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setId(1L);
        serviceRequest.setIssueDescription("Washing machine not working");
        serviceRequest.setHomeowner(homeowner);
        serviceRequest.setAppliance(appliance);

        List<ServiceRequest> requests = Arrays.asList(serviceRequest);

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(homeowner));
        when(serviceRequestRepository.findByHomeowner_Email("john@test.com")).thenReturn(requests);

        List<ServiceRequestResponseDto> result = serviceRequestService.getHomeownerRequests("john@test.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getHomeownerName());
    }

    @Test
    void testGetRequestById() {
        // Step 1: Create test data
        Homeowner homeowner = new Homeowner();
        homeowner.setId(1L);
        homeowner.setEmail("john@test.com");

        Appliance appliance = new Appliance();
        appliance.setId(1L);
        appliance.setSerialNumber("SN001");
        appliance.setBrand("Samsung");
        appliance.setModelNumber("WF45R6100AC");

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setId(1L);
        serviceRequest.setIssueDescription("Washing machine not working");
        serviceRequest.setHomeowner(homeowner);
        serviceRequest.setAppliance(appliance);

        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(serviceRequest));
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(homeowner));

        ServiceRequestResponseDto result = serviceRequestService.getRequestById(1L, "john@test.com");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Samsung WF45R6100AC (SN: SN001)", result.getApplianceInfo());
    }

    @Test
    void testUpdateServiceRequest() {
        Homeowner homeowner = new Homeowner();
        homeowner.setId(1L);
        homeowner.setEmail("john@test.com");

        Appliance appliance = new Appliance();
        appliance.setId(1L);
        appliance.setSerialNumber("SN001");
        appliance.setHomeowner(homeowner);

        ServiceRequest existingRequest = new ServiceRequest();
        existingRequest.setId(1L);
        existingRequest.setStatus(ServiceStatus.REQUESTED);
        existingRequest.setHomeowner(homeowner);
        existingRequest.setAppliance(appliance);

        ServiceRequestDto updateDto = new ServiceRequestDto();
        updateDto.setIssueDescription("Updated issue description");
        updateDto.setPreferredSlot(LocalDateTime.now().plusDays(2));

        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(existingRequest));
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(homeowner));
        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenReturn(existingRequest);

        ServiceRequestResponseDto result = serviceRequestService.updateRequest(1L, updateDto, "john@test.com");

        assertNotNull(result);
        verify(serviceRequestRepository).save(any(ServiceRequest.class));
    }

    @Test
    void testCancelServiceRequest() {
        //Create test data
        Homeowner homeowner = new Homeowner();
        homeowner.setId(1L);
        homeowner.setEmail("john@test.com");

        Appliance appliance = new Appliance();
        appliance.setId(1L);
        appliance.setSerialNumber("SN001");
        appliance.setHomeowner(homeowner);

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setId(1L);
        serviceRequest.setStatus(ServiceStatus.REQUESTED);
        serviceRequest.setHomeowner(homeowner);
        serviceRequest.setAppliance(appliance);

        //Set up mocks
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(serviceRequest));
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(homeowner));
        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenReturn(serviceRequest);


        serviceRequestService.cancelRequest(1L, "john@test.com");

        //Check that the save method was called
        verify(serviceRequestRepository).save(any(ServiceRequest.class));
    }

    @Test
    void testGetAllRequests() {
        //Create test data
        Homeowner homeowner = new Homeowner();
        homeowner.setId(1L);
        homeowner.setEmail("john@test.com");
        homeowner.setFirstName("John");
        homeowner.setLastName("Doe");

        Appliance appliance = new Appliance();
        appliance.setId(1L);
        appliance.setSerialNumber("SN001");
        appliance.setBrand("Samsung");

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setId(1L);
        serviceRequest.setIssueDescription("Washing machine not working");
        serviceRequest.setHomeowner(homeowner);
        serviceRequest.setAppliance(appliance);

        List<ServiceRequest> requests = Arrays.asList(serviceRequest);


        when(serviceRequestRepository.findAll()).thenReturn(requests);


        List<ServiceRequestResponseDto> result = serviceRequestService.getAllRequests();

        // Check the result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getHomeownerName());
    }
}