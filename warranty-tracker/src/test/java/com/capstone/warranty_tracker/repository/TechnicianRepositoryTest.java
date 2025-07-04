package com.capstone.warranty_tracker.repository;

import com.capstone.warranty_tracker.model.Technician;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TechnicianRepositoryTest {

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Test
    void testFindByEmail() {
        // Arrange: create and save technician
        Technician tech = new Technician();
        tech.setFirstName("Alice");
        tech.setLastName("Smith");
        tech.setEmail("alice@example.com");
        tech.setPhoneNumber("1234567890");
        tech.setSpecialization("Electrical");
        tech.setExperience(5);

        technicianRepository.save(tech);

        // Act: find by email
        Optional<Technician> found = technicianRepository.findByEmail("alice@example.com");

        // Assert: verify
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getFirstName());
        assertEquals("alice@example.com", found.get().getEmail());
    }
}
