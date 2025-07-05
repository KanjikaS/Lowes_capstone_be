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

    public HomeownerRegisterRequest(String u, String mail, String pw, String first, String last, String number, String addr) {
        super();
    }
    // Getters and setters
}