package com.capstone.warranty_tracker.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class HomeownerRegisterRequest extends BaseRegisterRequest {
    private String address;
    private String phoneNumber;
    // Getters and setters
}