package com.capstone.warranty_tracker.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletionFormDto {
    private String completionDate;
    private String completionTime;
    private String technicianNotes;
    private boolean confirmed;
}
