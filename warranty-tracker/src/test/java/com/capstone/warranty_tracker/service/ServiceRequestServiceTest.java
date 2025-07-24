package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.ServiceRequestDto;
import com.capstone.warranty_tracker.dto.ServiceRequestResponseDto;
import com.capstone.warranty_tracker.model.*;
import com.capstone.warranty_tracker.repository.ApplianceRepository;
import com.capstone.warranty_tracker.repository.HomeownerRepository;
import com.capstone.warranty_tracker.repository.ServiceRequestRepository;
import com.capstone.warranty_tracker.repository.TechnicianRepository;
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
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.junit.jupiter.api.BeforeEach;
import com.capstone.warranty_tracker.dto.AdminScheduleDto;
import com.capstone.warranty_tracker.dto.ServiceHistoryDto;
import java.time.LocalDate;
import static org.mockito.ArgumentMatchers.eq;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplianceRepository applianceRepository;

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private HomeownerRepository homeownerRepository;

    @Mock
    private TechnicianRepository technicianRepository;

    // @InjectMocks
    @Spy
    private ServiceRequestService serviceRequestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(serviceRequestService, "userRepository", userRepository);
        ReflectionTestUtils.setField(serviceRequestService, "applianceRepository", applianceRepository);
        ReflectionTestUtils.setField(serviceRequestService, "homeownerRepository", homeownerRepository);
        ReflectionTestUtils.setField(serviceRequestService, "serviceRequestRepository", serviceRequestRepository);
        ReflectionTestUtils.setField(serviceRequestService, "technicianRepository", technicianRepository);
    }

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
    void testCreateServiceRequest_InvalidPreferredSlot() {
        Homeowner homeowner = new Homeowner();
        homeowner.setId(1L);
        homeowner.setEmail("john@test.com");
        Appliance appliance = new Appliance();
        appliance.setId(1L);
        appliance.setSerialNumber("SN001");
        appliance.setHomeowner(homeowner);
        ServiceRequestDto requestDto = new ServiceRequestDto();
        requestDto.setSerialNumber("SN001");
        requestDto.setIssueDescription("Washing machine not working");
        requestDto.setPreferredSlot(LocalDateTime.now().minusDays(1));
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(homeowner));
        when(applianceRepository.findBySerialNumber("SN001")).thenReturn(Optional.of(appliance));
        assertThrows(IllegalArgumentException.class, () ->
            serviceRequestService.createRequest(requestDto, "john@test.com")
        );
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
    void testGetHomeownerRequests_HomeownerNotFound() {
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () ->
            serviceRequestService.getHomeownerRequests("notfound@test.com")
        );
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
    void testGetRequestById_ServiceRequestNotFound() {
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
            serviceRequestService.getRequestById(1L, "john@test.com")
        );
    }

    @Test
    void testGetRequestById_HomeownerNotFound() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () ->
            serviceRequestService.getRequestById(1L, "notfound@test.com")
        );
    }

    @Test
    void testGetRequestById_AccessDenied() {
        Homeowner homeowner = new Homeowner();
        homeowner.setEmail("owner@test.com");
        Homeowner other = new Homeowner();
        other.setEmail("other@test.com");
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setHomeowner(other);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(homeowner));
        assertThrows(AccessDeniedException.class, () ->
            serviceRequestService.getRequestById(1L, "owner@test.com")
        );
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
    void testUpdateRequest_AccessDenied() {
        Homeowner homeowner = new Homeowner();
        homeowner.setEmail("owner@test.com");
        Homeowner other = new Homeowner();
        other.setEmail("other@test.com");
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setHomeowner(other);
        sr.setStatus(ServiceStatus.REQUESTED);
        ServiceRequestDto dto = new ServiceRequestDto();
        dto.setPreferredSlot(LocalDateTime.now().plusDays(1));
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(homeowner));
        assertThrows(AccessDeniedException.class, () ->
            serviceRequestService.updateRequest(1L, dto, "owner@test.com")
        );
    }

    @Test
    void testUpdateRequest_StatusNotRequested() {
        Homeowner homeowner = new Homeowner();
        homeowner.setEmail("owner@test.com");
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setHomeowner(homeowner);
        sr.setStatus(ServiceStatus.ASSIGNED);
        ServiceRequestDto dto = new ServiceRequestDto();
        dto.setPreferredSlot(LocalDateTime.now().plusDays(1));
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(homeowner));
        assertThrows(IllegalStateException.class, () ->
            serviceRequestService.updateRequest(1L, dto, "owner@test.com")
        );
    }

    @Test
    void testUpdateRequest_InvalidPreferredSlot() {
        Homeowner homeowner = new Homeowner();
        homeowner.setEmail("owner@test.com");
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setHomeowner(homeowner);
        sr.setStatus(ServiceStatus.REQUESTED);
        ServiceRequestDto dto = new ServiceRequestDto();
        dto.setPreferredSlot(LocalDateTime.now().minusDays(1));
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(homeowner));
        assertThrows(IllegalArgumentException.class, () ->
            serviceRequestService.updateRequest(1L, dto, "owner@test.com")
        );
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
    void testCancelRequest_AccessDenied() {
        Homeowner homeowner = new Homeowner();
        homeowner.setEmail("owner@test.com");
        Homeowner other = new Homeowner();
        other.setEmail("other@test.com");
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setHomeowner(other);
        sr.setStatus(ServiceStatus.REQUESTED);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(homeowner));
        assertThrows(AccessDeniedException.class, () ->
            serviceRequestService.cancelRequest(1L, "owner@test.com")
        );
    }

    @Test
    void testCancelRequest_StatusNotRequested() {
        Homeowner homeowner = new Homeowner();
        homeowner.setEmail("owner@test.com");
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setHomeowner(homeowner);
        sr.setStatus(ServiceStatus.ASSIGNED);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(homeowner));
        assertThrows(IllegalStateException.class, () ->
            serviceRequestService.cancelRequest(1L, "owner@test.com")
        );
    }

    @Test
    void testCancelRequest_ServiceRequestNotFound() {
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
            serviceRequestService.cancelRequest(1L, "owner@test.com")
        );
    }

    @Test
    void testCancelRequest_HomeownerNotFound() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setHomeowner(new Homeowner());
        sr.setStatus(ServiceStatus.REQUESTED);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () ->
            serviceRequestService.cancelRequest(1L, "owner@test.com")
        );
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

    @Test
    void testTechnicianUpdateStatus_Success() {
        Technician technician = new Technician();
        technician.setId(1L);
        technician.setEmail("tech@example.com");

        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setTechnician(technician);
        sr.setStatus(ServiceStatus.REQUESTED);

        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));

        serviceRequestService.technicianUpdateStatus(1L, ServiceStatus.IN_PROGRESS, "tech@example.com");
        assertEquals(ServiceStatus.IN_PROGRESS, sr.getStatus());
    }

    @Test
    void testTechnicianUpdateStatus_Failure_NotAssigned() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setTechnician(null);

        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));

        assertThrows(RuntimeException.class, () ->
            serviceRequestService.technicianUpdateStatus(1L, ServiceStatus.IN_PROGRESS, "tech@example.com")
        );
    }

    @Test
    void testTechnicianUpdateStatus_Failure_WrongTechnician() {
        Technician technician = new Technician();
        technician.setId(1L);
        technician.setEmail("other@example.com");

        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setTechnician(technician);

        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));

        assertThrows(RuntimeException.class, () ->
            serviceRequestService.technicianUpdateStatus(1L, ServiceStatus.IN_PROGRESS, "tech@example.com")
        );
    }

    @Test
    void testTechnicianReschedule_Success() {
        Technician technician = new Technician();
        technician.setId(1L);
        technician.setEmail("tech@example.com");

        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setTechnician(technician);
        sr.setStatus(ServiceStatus.ASSIGNED);

        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        // slotTaken returns false
        doReturn(false).when(serviceRequestService).slotTaken(eq(1L), any(LocalDateTime.class));

        LocalDateTime newSlot = LocalDateTime.now().plusDays(2);
        serviceRequestService.technicianReschedule(1L, newSlot, "tech@example.com");
        assertEquals(newSlot, sr.getPreferredSlot());
        assertEquals(ServiceStatus.RESCHEDULED, sr.getStatus());
    }

    @Test
    void testTechnicianReschedule_Failure_SlotTaken() {
        Technician technician = new Technician();
        technician.setId(1L);
        technician.setEmail("tech@example.com");

        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setTechnician(technician);

        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        // slotTaken returns true
        doReturn(true).when(serviceRequestService).slotTaken(eq(1L), any(LocalDateTime.class));

        assertThrows(RuntimeException.class, () ->
            serviceRequestService.technicianReschedule(1L, LocalDateTime.now().plusDays(2), "tech@example.com")
        );
    }

    @Test
    void testTechnicianReschedule_Failure_WrongTechnician() {
        Technician technician = new Technician();
        technician.setId(1L);
        technician.setEmail("other@example.com");

        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setTechnician(technician);

        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));

        assertThrows(RuntimeException.class, () ->
            serviceRequestService.technicianReschedule(1L, LocalDateTime.now().plusDays(2), "tech@example.com")
        );
    }

    @Test
    void testAdminApprove_Success() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setStatus(ServiceStatus.REQUESTED);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        serviceRequestService.adminApprove(1L);
        assertEquals(ServiceStatus.ASSIGNED, sr.getStatus());
    }

    @Test
    void testUpdateStatus_Success() {
        Technician technician = new Technician();
        technician.setId(1L);
        technician.setEmail("tech@example.com");
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setTechnician(technician);
        sr.setStatus(ServiceStatus.ASSIGNED);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        serviceRequestService.updateStatus(1L, ServiceStatus.IN_PROGRESS, "tech@example.com");
        assertEquals(ServiceStatus.IN_PROGRESS, sr.getStatus());
    }

    @Test
    void testUpdateStatus_Failure_NotAssigned() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setTechnician(null);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        assertThrows(RuntimeException.class, () ->
            serviceRequestService.updateStatus(1L, ServiceStatus.IN_PROGRESS, "tech@example.com")
        );
    }

    @Test
    void testUpdateStatus_Failure_WrongTechnician() {
        Technician technician = new Technician();
        technician.setId(1L);
        technician.setEmail("other@example.com");
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setTechnician(technician);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        assertThrows(RuntimeException.class, () ->
            serviceRequestService.updateStatus(1L, ServiceStatus.IN_PROGRESS, "tech@example.com")
        );
    }

    @Test
    void testAvailableSlots() {
        LocalDate day = LocalDate.now().plusDays(1);
        Long techId = 1L;
        // All slots available
        doReturn(false).when(serviceRequestService).slotTaken(eq(techId), any(LocalDateTime.class));
        List<LocalDateTime> slots = serviceRequestService.availableSlots(techId, day);
        assertEquals(9, slots.size());
    }

    @Test
    void testReschedule_Success() {
        Homeowner homeowner = new Homeowner();
        homeowner.setEmail("user@example.com");
        Technician technician = new Technician();
        technician.setId(1L);
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setHomeowner(homeowner);
        sr.setTechnician(technician);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        doReturn(false).when(serviceRequestService).slotTaken(eq(1L), any(LocalDateTime.class));
        LocalDateTime newSlot = LocalDateTime.now().plusDays(2);
        serviceRequestService.reschedule(1L, newSlot, "user@example.com");
        assertEquals(newSlot, sr.getPreferredSlot());
        assertEquals(ServiceStatus.RESCHEDULED, sr.getStatus());
    }

    @Test
    void testReschedule_Failure_WrongHomeowner() {
        Homeowner homeowner = new Homeowner();
        homeowner.setEmail("other@example.com");
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setHomeowner(homeowner);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        assertThrows(RuntimeException.class, () ->
            serviceRequestService.reschedule(1L, LocalDateTime.now().plusDays(2), "user@example.com")
        );
    }

    @Test
    void testReschedule_Failure_TechnicianSlotTaken() {
        Homeowner homeowner = new Homeowner();
        homeowner.setEmail("user@example.com");
        Technician technician = new Technician();
        technician.setId(1L);
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setHomeowner(homeowner);
        sr.setTechnician(technician);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        doReturn(true).when(serviceRequestService).slotTaken(eq(1L), any(LocalDateTime.class));
        assertThrows(RuntimeException.class, () ->
            serviceRequestService.reschedule(1L, LocalDateTime.now().plusDays(2), "user@example.com")
        );
    }

    @Test
    void testAdminCreate_Success() {
        Homeowner homeowner = new Homeowner();
        homeowner.setId(1L);
        Appliance appliance = new Appliance();
        appliance.setId(2L);
        Technician technician = new Technician();
        technician.setId(3L);
        AdminScheduleDto dto = new AdminScheduleDto();
        dto.setHomeownerId(1L);
        dto.setApplianceId(2L);
        dto.setTechnicianId(3L);
        dto.setPreferredSlot(LocalDateTime.now().plusDays(1));
        dto.setIssueDescription("Test issue");
        when(homeownerRepository.findById(1L)).thenReturn(Optional.of(homeowner));
        when(applianceRepository.findById(2L)).thenReturn(Optional.of(appliance));
        when(technicianRepository.findById(3L)).thenReturn(Optional.of(technician));
        doReturn(false).when(serviceRequestService).slotTaken(eq(3L), any(LocalDateTime.class));
        serviceRequestService.adminCreate(dto);
        verify(serviceRequestRepository).save(any(ServiceRequest.class));
    }

    @Test
    void testAdminCreate_Failure_SlotTaken() {
        Homeowner homeowner = new Homeowner();
        homeowner.setId(1L);
        Appliance appliance = new Appliance();
        appliance.setId(2L);
        Technician technician = new Technician();
        technician.setId(3L);
        AdminScheduleDto dto = new AdminScheduleDto();
        dto.setHomeownerId(1L);
        dto.setApplianceId(2L);
        dto.setTechnicianId(3L);
        dto.setPreferredSlot(LocalDateTime.now().plusDays(1));
        dto.setIssueDescription("Test issue");
        when(homeownerRepository.findById(1L)).thenReturn(Optional.of(homeowner));
        when(applianceRepository.findById(2L)).thenReturn(Optional.of(appliance));
        when(technicianRepository.findById(3L)).thenReturn(Optional.of(technician));
        doReturn(true).when(serviceRequestService).slotTaken(eq(3L), any(LocalDateTime.class));
        assertThrows(RuntimeException.class, () ->
            serviceRequestService.adminCreate(dto)
        );
    }

    @Test
    void testAssignTechnician_Success() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setStatus(ServiceStatus.REQUESTED);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenReturn(sr);
        ServiceRequestResponseDto dto = serviceRequestService.assignTechnician(1L, 2L);
        assertEquals(ServiceStatus.ASSIGNED, sr.getStatus());
        assertNotNull(dto);
    }

    @Test
    void testAssignTechnician_Failure_WrongStatus() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setStatus(ServiceStatus.ASSIGNED);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sr));
        assertThrows(IllegalStateException.class, () ->
            serviceRequestService.assignTechnician(1L, 2L)
        );
    }

    @Test
    void testAssignTechnician_ServiceRequestNotFound() {
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
            serviceRequestService.assignTechnician(1L, 2L)
        );
    }

    @Test
    void testGetServiceHistoryByAppliance() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setIssueDescription("Test issue");
        sr.setStatus(ServiceStatus.COMPLETED);
        when(serviceRequestRepository.findByApplianceId(1L)).thenReturn(List.of(sr));
        List<ServiceHistoryDto> result = serviceRequestService.getServiceHistoryByAppliance(1L);
        assertEquals(1, result.size());
        assertEquals("Test issue", result.get(0).getIssueDescription());
    }

    @Test
    void testGetServiceHistoryByUsername() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setIssueDescription("Test issue");
        sr.setStatus(ServiceStatus.COMPLETED);
        when(serviceRequestRepository.findByHomeowner_Username("user1")).thenReturn(List.of(sr));
        List<ServiceHistoryDto> result = serviceRequestService.getServiceHistoryByUsername("user1");
        assertEquals(1, result.size());
        assertEquals("Test issue", result.get(0).getIssueDescription());
    }

    @Test
    void testGetServiceHistoryByTechnician_Id() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setIssueDescription("Test issue");
        sr.setStatus(ServiceStatus.COMPLETED);
        when(serviceRequestRepository.findByTechnician_Id(2L)).thenReturn(List.of(sr));
        List<ServiceHistoryDto> result = serviceRequestService.getServiceHistoryByTechnician_Id(2L);
        assertEquals(1, result.size());
        assertEquals("Test issue", result.get(0).getIssueDescription());
    }
}