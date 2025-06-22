package com.capstone.warranty_tracker.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceRequestDto {
    private Long applianceId;
    private String issueDescription;
    private LocalDateTime preferredSlot;
}

