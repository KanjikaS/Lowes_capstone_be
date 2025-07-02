package com.capstone.warranty_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ServiceRequestAdminDto {
    private Long id;
    private String applianceName;
    private String serialNumber;
    private String homeownerName;
    private String technicianName;
    private String status;
    private LocalDateTime createdAt;
}