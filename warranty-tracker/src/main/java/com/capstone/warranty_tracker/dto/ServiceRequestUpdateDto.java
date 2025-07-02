package com.capstone.warranty_tracker.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ServiceRequestUpdateDto {
    private LocalDateTime newSlot;
}