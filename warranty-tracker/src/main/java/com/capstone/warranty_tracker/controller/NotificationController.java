package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.ApplianceResponseDto;
import com.capstone.warranty_tracker.model.Appliance;
import com.capstone.warranty_tracker.repository.ApplianceRepository;
import com.capstone.warranty_tracker.service.WarrantyExpirySchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private WarrantyExpirySchedulerService warrantyExpirySchedulerService;

    @Autowired
    private ApplianceRepository applianceRepository;

    /**
     * Manual trigger for warranty expiry check (Admin only)
     */
    @PostMapping("/check-warranty-expiry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> triggerWarrantyCheck() {
        try {
            warrantyExpirySchedulerService.triggerManualWarrantyCheck();
            return ResponseEntity.ok("Warranty expiry check triggered successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error triggering warranty check: " + e.getMessage());
        }
    }

    /**
     * Check warranties expiring in specific number of days (Admin only)
     */
    @PostMapping("/check-warranty-expiry/{days}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> triggerWarrantyCheckInDays(@PathVariable int days) {
        try {
            warrantyExpirySchedulerService.checkWarrantyExpiryInDays(days);
            return ResponseEntity.ok("Warranty expiry check for " + days + " days triggered successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error triggering warranty check: " + e.getMessage());
        }
    }

    /**
     * Get appliances with warranties expiring in next 7 days (Admin only)
     */
    @GetMapping("/expiring-soon")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApplianceResponseDto>> getExpiringSoonAppliances() {
        try {
            LocalDate sevenDaysFromNow = LocalDate.now().plusDays(7);
            List<Appliance> expiringAppliances = applianceRepository.findByWarrantyExpiryDate(sevenDaysFromNow);
            
            List<ApplianceResponseDto> response = expiringAppliances.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get appliances with warranties expiring in specific number of days (Admin only)
     */
    @GetMapping("/expiring-in/{days}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApplianceResponseDto>> getAppliancesExpiringInDays(@PathVariable int days) {
        try {
            LocalDate targetDate = LocalDate.now().plusDays(days);
            List<Appliance> expiringAppliances = applianceRepository.findByWarrantyExpiryDate(targetDate);
            
            List<ApplianceResponseDto> response = expiringAppliances.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all appliances with warranties expiring in the next 30 days (Admin only)
     */
    @GetMapping("/expiring-next-month")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApplianceResponseDto>> getAppliancesExpiringNextMonth() {
        try {
            LocalDate now = LocalDate.now();
            LocalDate thirtyDaysFromNow = now.plusDays(30);
            
            List<Appliance> allAppliances = applianceRepository.findAll();
            List<Appliance> expiringAppliances = allAppliances.stream()
                    .filter(appliance -> {
                        LocalDate expiryDate = appliance.getWarrantyExpiryDate();
                        return expiryDate != null && 
                               !expiryDate.isBefore(now) && 
                               !expiryDate.isAfter(thirtyDaysFromNow);
                    })
                    .collect(Collectors.toList());
            
            List<ApplianceResponseDto> response = expiringAppliances.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ApplianceResponseDto convertToDto(Appliance appliance) {
        ApplianceResponseDto dto = new ApplianceResponseDto();
        dto.setId(appliance.getId());
        dto.setBrand(appliance.getBrand());
        dto.setModelNumber(appliance.getModelNumber());
        dto.setSerialNumber(appliance.getSerialNumber());
        dto.setPurchaseDate(appliance.getPurchaseDate());
        dto.setInvoiceUrl(appliance.getInvoiceUrl());
        dto.setWarrantyExpiryDate(appliance.getWarrantyExpiryDate());
        dto.setHomeownerName(appliance.getHomeowner().getFirstName() + " " + appliance.getHomeowner().getLastName());
        return dto;
    }
} 