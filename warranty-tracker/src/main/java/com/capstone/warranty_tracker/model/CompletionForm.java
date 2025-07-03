package com.capstone.warranty_tracker.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
public class CompletionForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String completionDate;
    private String completionTime;
    private String technicianNotes;
    private boolean confirmed;

    @OneToOne
    @JoinColumn(name = "service_request_id")
    private ServiceRequest serviceRequest;

    @ManyToOne(optional = false)  // ✅ this makes technician_id NOT NULL
    @JoinColumn(name = "technician_id")
    private Technician technician;
}
