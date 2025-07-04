package com.capstone.warranty_tracker.repository;

import com.capstone.warranty_tracker.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class CompletionFormRepositoryTest {

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CompletionFormRepository completionFormRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private HomeownerRepository homeownerRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private ApplianceRepository applianceRepository;

    @Test
    void testFindByServiceRequest_Id() {
        // Create and save Homeowner
        Homeowner homeowner = new Homeowner();
        homeowner.setFirstName("John");
        homeowner.setLastName("Doe");
        homeowner.setEmail("john.doe@example.com");
        homeowner.setPhoneNumber("1234567890");
        homeowner.setAddress("123 Main St");
        homeowner = homeownerRepository.save(homeowner);

        // Create and save Appliance
        Appliance appliance = new Appliance();
        appliance.setBrand("LG");
        appliance.setCategory("Refrigerator");
        appliance.setModelNumber("LG-123");
        appliance.setSerialNumber("SN12345678");
        appliance.setPurchaseDate(LocalDate.now().minusYears(1));
        appliance.setInvoiceUrl("invoice.pdf");
        appliance.setWarrantyExpiryDate(LocalDate.now().plusYears(1));
        appliance.setHomeowner(homeowner);
        appliance = applianceRepository.save(appliance);

        // Create and save Technician
        Technician technician = new Technician();
        technician.setFirstName("Tech");
        technician.setLastName("Smith");
        technician.setEmail("tech@example.com");
        technician.setPhoneNumber("9876543210");
        technician.setSpecialization("Cooling Systems");
        technician.setExperience(5);
        technician = technicianRepository.save(technician);

        // Create and save ServiceRequest
        ServiceRequest request = new ServiceRequest();
        request.setIssueDescription("Fix refrigerator");
        request.setPreferredSlot(LocalDateTime.now().plusDays(1));
        request.setStatus(ServiceStatus.ASSIGNED);
        request.setHomeowner(homeowner);
        request.setAppliance(appliance);
        request.setTechnician(technician);
        request.setCreatedAt(LocalDateTime.now());
        request = serviceRequestRepository.save(request);

        // Create and save CompletionForm
        CompletionForm form = new CompletionForm();
        form.setServiceRequest(request);
        form.setCompletionDate("2024-07-04");
        form.setCompletionTime("14:00");
        form.setTechnicianNotes("Replaced compressor");
        form.setConfirmed(true);
        form.setTechnician(technician);

        completionFormRepository.save(form);

        // Test findByServiceRequest_Id
        CompletionForm found = completionFormRepository.findByServiceRequest_Id(request.getId());

        assertNotNull(found);
        assertEquals(request.getId(), found.getServiceRequest().getId());
        assertEquals("Replaced compressor", found.getTechnicianNotes());
    }
}
