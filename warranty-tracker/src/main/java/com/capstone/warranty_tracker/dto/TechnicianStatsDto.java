package com.capstone.warranty_tracker.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TechnicianStatsDto {
    private long assignedCount;
    private long inProgressCount;
    private long completedCount;
}
