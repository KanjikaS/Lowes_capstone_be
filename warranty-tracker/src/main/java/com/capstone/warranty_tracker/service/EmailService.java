package com.capstone.warranty_tracker.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String resetLink) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("serviceprolowes@gmail.com"); // Your application's email
        message.setTo(toEmail);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click on the following link: \n"
                + resetLink
                + "\n\nThis link will expire in 1 hour."); // Add expiration info
        mailSender.send(message);
    }
}
