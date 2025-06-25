package com.capstone.warranty_tracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
@Getter
@Setter
@Data
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
