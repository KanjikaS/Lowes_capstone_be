package com.capstone.warranty_tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
@Getter
@Setter

@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Appliance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String modelNumber;

    @Column(unique = true)
    private String serialNumber;

    private LocalDate purchaseDate;
    private String invoiceUrl;
    private LocalDate warrantyExpiryDate;


    @ManyToOne(optional = false)
    @JoinColumn(name = "homeowner_id", nullable = false) // ✅ Explicit mapping to correct column
    private Homeowner homeowner;

    @OneToMany(mappedBy = "appliance", cascade = CascadeType.ALL)
    private List<ServiceRequest> serviceRequests;
}
