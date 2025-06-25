package com.capstone.warranty_tracker.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceRequestDto {
    private String serialNumber;
    private String issueDescription;
    private LocalDateTime preferredSlot;
    private Long technicianId;
}

