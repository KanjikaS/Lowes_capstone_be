package com.capstone.warranty_tracker.repository;

import com.capstone.warranty_tracker.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class ServiceRequestRepositoryTest {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private HomeownerRepository homeownerRepository;

    @Autowired
    private ApplianceRepository applianceRepository;
    @Autowired
    private TechnicianRepository technicianRepository;


    private Homeowner testHomeowner1;
    private Homeowner testHomeowner2;
    private Appliance testAppliance1;
    private Appliance testAppliance2;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Create test homeowners
        testHomeowner1 = createHomeowner("john@example.com", "John", "Doe");
        testHomeowner2 = createHomeowner("jane@example.com", "Jane", "Smith");
        
        // Create test appliances
        testAppliance1 = createAppliance(testHomeowner1, "SN123456");
        testAppliance2 = createAppliance(testHomeowner2, "SN789012");
    }

    private Homeowner createHomeowner(String email, String firstName, String lastName) {
        Homeowner homeowner = new Homeowner();
        homeowner.setFirstName(firstName);
        homeowner.setLastName(lastName);
        homeowner.setEmail(email);
        homeowner.setPassword("password123");
        homeowner.setPhoneNumber("1234567890");
        homeowner.setAddress("123 Main Street");
        return homeownerRepository.save(homeowner);
    }

    private Appliance createAppliance(Homeowner homeowner, String serialNumber) {
        Appliance appliance = new Appliance();
        appliance.setBrand("Samsung");
        appliance.setCategory("Washer");
        appliance.setModelNumber("SM-900X");
        appliance.setSerialNumber(serialNumber);
        appliance.setPurchaseDate(LocalDateTime.now().toLocalDate());
        appliance.setInvoiceUrl("invoice123.pdf");
        appliance.setWarrantyExpiryDate(LocalDateTime.now().plusYears(2).toLocalDate());
        appliance.setHomeowner(homeowner);
        return applianceRepository.save(appliance);
    }

    private ServiceRequest createServiceRequest(Homeowner homeowner, Appliance appliance, String issueDescription) {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setIssueDescription(issueDescription);
        serviceRequest.setPreferredSlot(LocalDateTime.now().plusDays(1));
        serviceRequest.setStatus(ServiceStatus.REQUESTED);
        serviceRequest.setHomeowner(homeowner);
        serviceRequest.setAppliance(appliance);
        serviceRequest.setCreatedAt(LocalDateTime.now());
        return serviceRequestRepository.save(serviceRequest);
    }

    @Test
    void testFindByHomeowner_Email_shouldReturnServiceRequestsForSpecificEmail() {
        // Create service requests for homeowner1
        ServiceRequest request1 = createServiceRequest(testHomeowner1, testAppliance1, "Washing machine not working");
        ServiceRequest request2 = createServiceRequest(testHomeowner1, testAppliance1, "Dryer making strange noise");
        
        // Create service request for homeowner2
        ServiceRequest request3 = createServiceRequest(testHomeowner2, testAppliance2, "Dishwasher leaking");

        // Test finding service requests by homeowner1 email
        List<ServiceRequest> homeowner1Requests = serviceRequestRepository.findByHomeowner_Email("john@example.com");
        
        // Verify results
        assertEquals(2, homeowner1Requests.size());
        assertTrue(homeowner1Requests.stream()
                .allMatch(request -> request.getHomeowner().getEmail().equals("john@example.com")));
        assertTrue(homeowner1Requests.stream()
                .anyMatch(request -> request.getIssueDescription().equals("Washing machine not working")));
        assertTrue(homeowner1Requests.stream()
                .anyMatch(request -> request.getIssueDescription().equals("Dryer making strange noise")));
    }

    @Test
    void testFindByHomeowner_Email_shouldReturnEmptyListForNonExistentEmail() {
        // Create service requests for existing homeowner
        createServiceRequest(testHomeowner1, testAppliance1, "Washing machine not working");
        
        // Test finding service requests by non-existent email
        List<ServiceRequest> requests = serviceRequestRepository.findByHomeowner_Email("nonexistent@example.com");
        
        // Verify empty result
        assertTrue(requests.isEmpty());
    }

    @Test
    void testFindByHomeowner_Email_shouldReturnCorrectServiceRequestsForMultipleHomeowners() {
        // Create service requests for homeowner1
        ServiceRequest request1 = createServiceRequest(testHomeowner1, testAppliance1, "Washing machine not working");
        ServiceRequest request2 = createServiceRequest(testHomeowner1, testAppliance1, "Dryer making strange noise");
        
        // Create service request for homeowner2
        ServiceRequest request3 = createServiceRequest(testHomeowner2, testAppliance2, "Dishwasher leaking");

        // Test finding service requests for homeowner1
        List<ServiceRequest> homeowner1Requests = serviceRequestRepository.findByHomeowner_Email("john@example.com");
        assertEquals(2, homeowner1Requests.size());
        
        // Test finding service requests for homeowner2
        List<ServiceRequest> homeowner2Requests = serviceRequestRepository.findByHomeowner_Email("jane@example.com");
        assertEquals(1, homeowner2Requests.size());
        assertEquals("Dishwasher leaking", homeowner2Requests.get(0).getIssueDescription());
    }

    @Test
    void testFindByHomeowner_Email_shouldReturnServiceRequestsWithCorrectStatus() {
        // Create service requests with different statuses
        ServiceRequest request1 = createServiceRequest(testHomeowner1, testAppliance1, "Washing machine not working");
        request1.setStatus(ServiceStatus.REQUESTED);
        serviceRequestRepository.save(request1);
        
        ServiceRequest request2 = createServiceRequest(testHomeowner1, testAppliance1, "Dryer making strange noise");
        request2.setStatus(ServiceStatus.IN_PROGRESS);
        serviceRequestRepository.save(request2);
        
        ServiceRequest request3 = createServiceRequest(testHomeowner1, testAppliance1, "Oven not heating");
        request3.setStatus(ServiceStatus.COMPLETED);
        serviceRequestRepository.save(request3);

        // Test finding all service requests for homeowner1
        List<ServiceRequest> requests = serviceRequestRepository.findByHomeowner_Email("john@example.com");
        
        // Verify all requests are returned regardless of status
        assertEquals(3, requests.size());
        assertTrue(requests.stream().anyMatch(r -> r.getStatus() == ServiceStatus.REQUESTED));
        assertTrue(requests.stream().anyMatch(r -> r.getStatus() == ServiceStatus.IN_PROGRESS));
        assertTrue(requests.stream().anyMatch(r -> r.getStatus() == ServiceStatus.COMPLETED));
    }

    @Test
    void testFindByHomeowner_Email_shouldReturnServiceRequestsWithCorrectApplianceInfo() {
        // Create service requests with different appliances
        ServiceRequest request1 = createServiceRequest(testHomeowner1, testAppliance1, "Washing machine not working");
        ServiceRequest request2 = createServiceRequest(testHomeowner1, testAppliance1, "Dryer making strange noise");

        // Test finding service requests for homeowner1
        List<ServiceRequest> requests = serviceRequestRepository.findByHomeowner_Email("john@example.com");
        
        // Verify appliance information is correct
        assertEquals(2, requests.size());
        assertTrue(requests.stream().allMatch(r -> r.getAppliance().getSerialNumber().equals("SN123456")));
        assertTrue(requests.stream().allMatch(r -> r.getAppliance().getBrand().equals("Samsung")));
    }

    @Test
    void testFindAssignedRequestsByTechnicianEmail_shouldReturnRequestsAssignedToTechnician() {

        Technician technician = new Technician();
        technician.setFirstName("Alice");
        technician.setLastName("Brown");
        technician.setEmail("alice.technician@example.com");
        technician = technicianRepository.save(technician);

        ServiceRequest request1 = createServiceRequest(testHomeowner1, testAppliance1, "Washing machine not spinning");
        request1.setTechnician(technician);
        serviceRequestRepository.save(request1);

        ServiceRequest request2 = createServiceRequest(testHomeowner1, testAppliance1, "Dryer overheating");
        request2.setTechnician(technician);
        serviceRequestRepository.save(request2);

        ServiceRequest request3 = createServiceRequest(testHomeowner1, testAppliance1, "Dishwasher leaking");
        serviceRequestRepository.save(request3);

        List<ServiceRequest> assignedRequests = serviceRequestRepository.findAssignedRequestsByTechnicianEmail("alice.technician@example.com");

        assertEquals(2, assignedRequests.size());
        assertTrue(assignedRequests.stream().allMatch(sr -> sr.getTechnician().getEmail().equals("alice.technician@example.com")));
        assertTrue(assignedRequests.stream().anyMatch(sr -> sr.getIssueDescription().equals("Washing machine not spinning")));
        assertTrue(assignedRequests.stream().anyMatch(sr -> sr.getIssueDescription().equals("Dryer overheating")));
    }

} 