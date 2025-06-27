package com.capstone.warranty_tracker.dto;

import com.capstone.warranty_tracker.model.ServiceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHistoryDto {
    private Long id;
    private String issueDescription;
    private LocalDateTime serviceDate;
    private ServiceStatus status;
    private String homeownerName; // Just the name, not the full object
    private String applianceInfo; // Brand + Model + Serial, not the full object
}
