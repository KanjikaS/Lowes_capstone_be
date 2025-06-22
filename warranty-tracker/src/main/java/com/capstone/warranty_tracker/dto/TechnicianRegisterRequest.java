package com.capstone.warranty_tracker.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TechnicianRegisterRequest extends BaseRegisterRequest {
    private String phoneNumber;
    private String specialization;
    private int experience;
    // Getters and setters
}
