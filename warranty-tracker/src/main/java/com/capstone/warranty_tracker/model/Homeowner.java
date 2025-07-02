package com.capstone.warranty_tracker.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter

@AllArgsConstructor

@Entity
public class Homeowner extends User {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;

    public Homeowner() {
        this.setRole(Role.ROLE_HOMEOWNER);
    }

    @OneToMany(mappedBy = "homeowner", cascade = CascadeType.ALL)
    private List<Appliance> appliances;

    @OneToMany(mappedBy = "homeowner", cascade = CascadeType.ALL)
    private List<ServiceRequest> serviceRequests;

    // Additional profile fields if needed (address, phone, etc.)
}
