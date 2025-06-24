package com.capstone.warranty_tracker.dto;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TechnicianAssignmentResponseDto {
    private Long serviceRequestId;
    private Long technicianId;
    private String technicianName;
    private String message;
}