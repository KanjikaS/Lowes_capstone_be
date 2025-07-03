package com.capstone.warranty_tracker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompletionFormResponseDto {
    private Long id;
    private String completionDate;
    private String completionTime;
    private String technicianNotes;
    private boolean confirmed;

    private Long serviceRequestId;

    private Long technicianId;
    private String technicianEmail;
}
