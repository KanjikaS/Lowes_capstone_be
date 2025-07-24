package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.model.Appliance;
import com.capstone.warranty_tracker.model.Homeowner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    private Appliance appliance;
    private Homeowner homeowner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        homeowner = new Homeowner();
        homeowner.setEmail("vishalranka3@gmail.com");
        homeowner.setFirstName("Test");
        homeowner.setLastName("Homeowner");
        homeowner.setPhoneNumber("555-TEST-123");

        appliance = new Appliance();
        appliance.setBrand("Samsung");
        appliance.setModelNumber("RF28R7201SR");
        appliance.setSerialNumber("TEST123456");
        appliance.setWarrantyExpiryDate(java.time.LocalDate.now().plusDays(7));
        appliance.setHomeowner(homeowner);
    }

    @Test
    void testSendWarrantyExpiryNotification_Email() {
        notificationService.sendWarrantyExpiryNotification(appliance);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

} 