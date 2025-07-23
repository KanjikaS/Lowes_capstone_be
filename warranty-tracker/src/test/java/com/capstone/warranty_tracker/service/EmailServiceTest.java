package com.capstone.warranty_tracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendPasswordResetEmail_Success() {
        String toEmail = "test@example.com";
        String resetLink = "http://localhost:3000/reset?token=abc";

        // No explicit 'when' needed for void methods, just verify interaction
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendPasswordResetEmail(toEmail, resetLink);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals("serviceprolowes@gmail.com", capturedMessage.getFrom());
        assertEquals(toEmail, capturedMessage.getTo()[0]); // To is an array
        assertEquals("Password Reset Request", capturedMessage.getSubject());
        assertTrue(capturedMessage.getText().contains(resetLink));
        assertTrue(capturedMessage.getText().contains("This link will expire in 1 hour."));
    }

    @Test
    void sendPasswordResetEmail_MailException() {
        String toEmail = "test@example.com";
        String resetLink = "http://localhost:3000/reset?token=abc";

        // Simulate MailException during sending
        doThrow(new org.springframework.mail.MailSendException("Simulated mail send error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // The method declares throwing MailException, so we expect it
        MailException thrownException = assertThrows(MailException.class, () -> {
            emailService.sendPasswordResetEmail(toEmail, resetLink);
        });

        assertTrue(thrownException.getMessage().contains("Simulated mail send error"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}