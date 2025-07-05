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

    public TechnicianRegisterRequest(String t, String mail, String pw, String a, String b, String number, String spec, int i) {
        super();
    }
    // Getters and setters
}
