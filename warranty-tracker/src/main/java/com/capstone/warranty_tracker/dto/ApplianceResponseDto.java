package com.capstone.warranty_tracker.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ApplianceResponseDto {
    private Long id;
    private String brand;
    private String modelNumber;
    private String serialNumber;
    private LocalDate purchaseDate;
    private String invoiceUrl;
    private LocalDate warrantyExpiryDate;
    private String homeownerName; // Just the name, not the full object
}