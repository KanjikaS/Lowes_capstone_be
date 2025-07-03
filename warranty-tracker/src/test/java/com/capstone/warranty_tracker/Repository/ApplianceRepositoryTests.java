package com.capstone.warranty_tracker.repository;

import com.capstone.warranty_tracker.WarrantyTrackerApplication;
import com.capstone.warranty_tracker.model.Appliance;
import com.capstone.warranty_tracker.model.Homeowner;
import com.capstone.warranty_tracker.repository.ApplianceRepository;
import com.capstone.warranty_tracker.repository.HomeownerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ComponentScan(basePackages = "com.capstone.warranty_tracker",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WarrantyTrackerApplication.DataSeeder.class))
public class ApplianceRepositoryTests {

    @Autowired
    private ApplianceRepository applianceRepository;

    @Autowired
    private HomeownerRepository homeownerRepository;

    private Homeowner testHomeowner;

    @BeforeEach
    void setUp() {
        testHomeowner = createHomeowner();
    }

    private Homeowner createHomeowner() {
        Homeowner homeowner = new Homeowner();
        homeowner.setFirstName("John");
        homeowner.setLastName("Doe");
        homeowner.setEmail("john@example.com");
        homeowner.setPassword("password123");
        homeowner.setPhoneNumber("1234567890");
        homeowner.setAddress("123 Main Street");
        return homeownerRepository.save(homeowner);
    }

    private Appliance createAppliance(Homeowner homeowner) {
        Appliance appliance = new Appliance();
        appliance.setBrand("Samsung");
        appliance.setCategory("Washer");
        appliance.setModelNumber("SM-900X");
        appliance.setSerialNumber("SN12345678");
        appliance.setPurchaseDate(LocalDate.of(2024, 1, 15));
        appliance.setInvoiceUrl("invoice123.pdf");
        appliance.setWarrantyExpiryDate(LocalDate.of(2026, 1, 15));
        appliance.setHomeowner(homeowner);
        return applianceRepository.save(appliance);
    }

    @Test
    void testSaveAppliance_shouldReturnSavedApplianceWithId() {
        Appliance savedAppliance = createAppliance(testHomeowner);
        assertNotNull(savedAppliance.getId());
        assertEquals("Samsung", savedAppliance.getBrand());
    }

    @Test
    void testFindAllByHomeowner_shouldReturnAllAppliancesForHomeowner() {
        createAppliance(testHomeowner);

        List<Appliance> appliances = applianceRepository.findAll(); // or findAllByHomeowner(testHomeowner) if applicable
        assertNotNull(appliances);
        assertFalse(appliances.isEmpty());
        assertEquals(testHomeowner.getId(), appliances.get(0).getHomeowner().getId());
    }

    @Test
    void testDeleteAppliance_shouldRemoveApplianceFromDatabase() {
        Appliance appliance = createAppliance(testHomeowner);
        applianceRepository.delete(appliance);

        Optional<Appliance> deleted = applianceRepository.findById(appliance.getId());
        assertFalse(deleted.isPresent());
    }

    @Test
    void testFindById_shouldReturnCorrectAppliance() {
        Appliance savedAppliance = createAppliance(testHomeowner);
        Optional<Appliance> found = applianceRepository.findById(savedAppliance.getId());
        assertTrue(found.isPresent());
        assertEquals("Samsung", found.get().getBrand());
    }

    @Test
    void testFindBySerialNumber_shouldReturnCorrectAppliance() {
        createAppliance(testHomeowner);

        Optional<Appliance> found = applianceRepository.findBySerialNumber("SN12345678");
        assertTrue(found.isPresent());
        assertEquals("Samsung", found.get().getBrand());
        assertEquals("SN12345678", found.get().getSerialNumber());
    }
}

