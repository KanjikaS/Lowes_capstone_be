package com.capstone.warranty_tracker.dto;

import com.capstone.warranty_tracker.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseRegisterRequest {
    private String email;
    private String password;
    private String username;
    private Role role;
    private String firstName;
    private String lastName;
    // Getters and setters
}