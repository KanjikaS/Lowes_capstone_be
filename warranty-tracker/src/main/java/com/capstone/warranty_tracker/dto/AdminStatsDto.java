package com.capstone.warranty_tracker.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminStatsDto {
    private int totalTechnicians;
    private int pendingRequests;
    private int totalAppliances;
    private int completedRequests;

    public AdminStatsDto(int totalTechnicians, int pendingRequests, int totalAppliances, int completedRequests) {
        this.totalTechnicians = totalTechnicians;
        this.pendingRequests = pendingRequests;
        this.totalAppliances = totalAppliances;
        this.completedRequests = completedRequests;
    }
}
