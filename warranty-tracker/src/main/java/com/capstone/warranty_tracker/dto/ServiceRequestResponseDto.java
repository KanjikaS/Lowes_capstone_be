package com.capstone.warranty_tracker.dto;

import com.capstone.warranty_tracker.model.ServiceStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceRequestResponseDto {
    private Long id;
    private String issueDescription;
    private LocalDateTime preferredSlot;
    private ServiceStatus status;
    private String homeownerName; // Just the name, not the full object
    private String applianceInfo; // Brand + Model + Serial, not the full object
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}