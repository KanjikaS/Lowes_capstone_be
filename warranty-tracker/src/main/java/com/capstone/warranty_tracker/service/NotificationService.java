package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.model.Appliance;
import com.capstone.warranty_tracker.model.Homeowner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendWarrantyExpiryNotification(Appliance appliance) {
        Homeowner homeowner = appliance.getHomeowner();
        
        try {
            // Send email notification
            sendEmailNotification(homeowner, appliance);
            
            
            logger.info("Warranty expiry notification sent for appliance: {} to homeowner: {}", 
                       appliance.getSerialNumber(), homeowner.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send warranty expiry notification for appliance: {} to homeowner: {}", 
                        appliance.getSerialNumber(), homeowner.getEmail(), e);
        }
    }

    private void sendEmailNotification(Homeowner homeowner, Appliance appliance) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(homeowner.getEmail());
        message.setSubject("Warranty Expiry Alert - " + appliance.getBrand() + " " + appliance.getModelNumber());
        
        String emailBody = buildEmailBody(homeowner, appliance);
        message.setText(emailBody);
        
        mailSender.send(message);
        logger.info("Email notification sent to: {}", homeowner.getEmail());
    }

    private String buildEmailBody(Homeowner homeowner, Appliance appliance) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        String formattedDate = appliance.getWarrantyExpiryDate().format(formatter);
        
        return String.format(
            "Dear %s %s,\n\n" +
            "This is a friendly reminder that the warranty for your %s %s (Serial Number: %s) " +
            "will expire on %s.\n\n" +
            "We recommend:\n" +
            "• Review your appliance's current condition\n" +
            "• Consider scheduling a maintenance check if needed\n" +
            "• Keep your warranty documentation safe\n" +
            "• Contact us if you have any warranty-related questions\n\n" +
            "If you need any assistance or have questions about your warranty, " +
            "please don't hesitate to contact our support team.\n\n" +
            "Best regards,\n" +
            "Lowe's Warranty Tracker Team\n\n" +
            "Note: This is an automated message. Please do not reply to this email.",
            homeowner.getFirstName(),
            homeowner.getLastName(),
            appliance.getBrand(),
            appliance.getModelNumber(),
            appliance.getSerialNumber(),
            formattedDate
        );
    }

} 