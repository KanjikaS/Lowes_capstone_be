package com.capstone.warranty_tracker.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ApplianceRequestDto {
    private String brand;
    private String modelNumber;
    private LocalDate purchaseDate;
    private String invoiceUrl;
    private LocalDate warrantyExpiryDate;
}