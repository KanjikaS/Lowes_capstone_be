package com.capstone.warranty_tracker.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter


@Entity
public class Admin extends User {
    private String firstName;
    private String lastName;
    public Admin() {
        this.setRole(Role.ROLE_ADMIN);
    }
}