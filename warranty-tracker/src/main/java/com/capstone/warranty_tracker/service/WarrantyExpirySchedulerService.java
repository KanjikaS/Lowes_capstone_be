package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.model.Appliance;
import com.capstone.warranty_tracker.repository.ApplianceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@Service
public class WarrantyExpirySchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(WarrantyExpirySchedulerService.class);

    @Autowired
    private ApplianceRepository applianceRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Scheduled method that runs daily at 9:00 AM to check for warranties expiring in 7 days
     * Cron expression: "0 0 9 * * ?" means:
     * - 0 seconds
     * - 0 minutes  
     * - 9 hours (9 AM)
     * - every day of month
     * - every month
     * - any day of week
     */
    @Scheduled(cron = "0 54 16 * * ?")
    public void checkWarrantyExpiry() {
        logger.info("Starting warranty expiry check scheduled task...");
        
        LocalDate sevenDaysFromNow = LocalDate.now().plusDays(7);
        
        try {
            List<Appliance> expiringAppliances = applianceRepository.findByWarrantyExpiryDate(sevenDaysFromNow);
            
            logger.info("Found {} appliances with warranty expiring on {}", 
                       expiringAppliances.size(), sevenDaysFromNow);
            
            for (Appliance appliance : expiringAppliances) {
                try {
                    notificationService.sendWarrantyExpiryNotification(appliance);
                    logger.info("Notification sent for appliance: {} (Serial: {})", 
                               appliance.getBrand() + " " + appliance.getModelNumber(), 
                               appliance.getSerialNumber());
                } catch (Exception e) {
                    logger.error("Failed to send notification for appliance: {} (Serial: {})", 
                                appliance.getBrand() + " " + appliance.getModelNumber(), 
                                appliance.getSerialNumber(), e);
                }
            }
            
            logger.info("Warranty expiry check completed. Processed {} appliances", expiringAppliances.size());
        } catch (Exception e) {
            logger.error("Error during warranty expiry check", e);
        }
    }

    /**
     * Manual trigger method for testing purposes
     * Can be called via API endpoint for immediate execution
     */
    public void triggerManualWarrantyCheck() {
        logger.info("Manual warranty expiry check triggered");
        checkWarrantyExpiry();
    }

    /**
     * Method to check warranties expiring in a specific number of days
     * Useful for testing or different notification schedules
     */
    public void checkWarrantyExpiryInDays(int days) {
        logger.info("Checking warranties expiring in {} days", days);
        
        LocalDate targetDate = LocalDate.now().plusDays(days);
        
        try {
            List<Appliance> expiringAppliances = applianceRepository.findByWarrantyExpiryDate(targetDate);
            
            logger.info("Found {} appliances with warranty expiring on {}", 
                       expiringAppliances.size(), targetDate);
            
            for (Appliance appliance : expiringAppliances) {
                notificationService.sendWarrantyExpiryNotification(appliance);
            }
            
        } catch (Exception e) {
            logger.error("Error during warranty expiry check for {} days", days, e);
        }
    }
} 