package com.capstone.warranty_tracker.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianAssignmentWrapper {
    private String message;
    private List<TechnicianAssignmentResponseDto> assignments;
}
