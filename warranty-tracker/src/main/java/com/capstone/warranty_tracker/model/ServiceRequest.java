package com.capstone.warranty_tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter

@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ServiceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String issueDescription;
    private LocalDateTime preferredSlot;
    private LocalDateTime scheduledSlot;

    @Enumerated(EnumType.STRING)
    private ServiceStatus status;

    @ManyToOne
    private Homeowner homeowner;

    @ManyToOne
    private Appliance appliance;

    @ManyToOne
    private Technician technician;

    private LocalDateTime createdAt;
}






