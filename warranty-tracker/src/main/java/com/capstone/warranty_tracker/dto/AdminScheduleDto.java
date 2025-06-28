package com.capstone.warranty_tracker.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminScheduleDto {
    private Long homeownerId;
    private Long applianceId;
    private Long technicianId;
    private String issueDescription;
    private LocalDateTime preferredSlot;
}