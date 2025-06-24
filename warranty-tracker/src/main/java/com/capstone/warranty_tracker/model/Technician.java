package com.capstone.warranty_tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private String specialization; // e.g., "HVAC, Electronics"
    private int experience;
    public Technician() {
        this.setRole(Role.ROLE_TECHNICIAN);
    }

    @OneToMany(mappedBy = "technician", cascade = CascadeType.ALL)
    private List<ServiceRequest> assignedRequests;

    @ManyToOne
    @JoinColumn(name = "technician_id")
    private Technician technician;



}
