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
public class Technician extends User {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String specialization;
    private int experience;
    public Technician() {
        this.setRole(Role.ROLE_TECHNICIAN);
    }

    @OneToMany(mappedBy = "technician", cascade = CascadeType.ALL)
    private List<ServiceRequest> assignedRequests;


}
