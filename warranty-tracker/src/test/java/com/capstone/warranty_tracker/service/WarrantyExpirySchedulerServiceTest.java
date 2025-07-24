package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.model.Appliance;
import com.capstone.warranty_tracker.repository.ApplianceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class WarrantyExpirySchedulerServiceTest {

    @Mock
    private ApplianceRepository applianceRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private WarrantyExpirySchedulerService schedulerService;

    private Appliance appliance;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appliance = new Appliance();
        appliance.setBrand("Samsung");
        appliance.setModelNumber("RF28R7201SR");
        appliance.setSerialNumber("TEST123456");
        appliance.setWarrantyExpiryDate(LocalDate.now().plusDays(7));
    }

    @Test
    void testCheckWarrantyExpiry_ScheduledTask() {
        when(applianceRepository.findByWarrantyExpiryDate(LocalDate.now().plusDays(7)))
                .thenReturn(Collections.singletonList(appliance));

        schedulerService.checkWarrantyExpiry();

        verify(notificationService, times(1)).sendWarrantyExpiryNotification(appliance);
    }

    @Test
    void testTriggerManualWarrantyCheck() {
        when(applianceRepository.findByWarrantyExpiryDate(LocalDate.now().plusDays(7)))
                .thenReturn(Collections.singletonList(appliance));

        schedulerService.triggerManualWarrantyCheck();

        verify(notificationService, times(1)).sendWarrantyExpiryNotification(appliance);
    }

    @Test
    void testCheckWarrantyExpiryInDays() {
        int days = 10;
        when(applianceRepository.findByWarrantyExpiryDate(LocalDate.now().plusDays(days)))
                .thenReturn(Collections.singletonList(appliance));

        schedulerService.checkWarrantyExpiryInDays(days);

        verify(notificationService, times(1)).sendWarrantyExpiryNotification(appliance);
    }
} 