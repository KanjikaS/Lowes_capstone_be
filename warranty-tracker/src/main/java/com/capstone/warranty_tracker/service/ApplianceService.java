package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.ApplianceRequestDto;
import com.capstone.warranty_tracker.dto.ApplianceResponseDto;
import com.capstone.warranty_tracker.model.Appliance;
import com.capstone.warranty_tracker.model.Homeowner;
import com.capstone.warranty_tracker.repository.ApplianceRepository;
import com.capstone.warranty_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplianceService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplianceRepository applianceRepository;

    public void addAppliance(ApplianceRequestDto dto, String email) {
        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));

        System.out.println("Adding appliance for homeowner: " + homeowner.getFirstName() + " " + homeowner.getLastName());
        System.out.println("Homeowner ID: " + homeowner.getId());

        // Check if serial number already exists
        if (applianceRepository.existsBySerialNumber(dto.getSerialNumber())) {
            throw new IllegalArgumentException("Appliance with serial number '" + dto.getSerialNumber() + "' already exists. Serial numbers must be unique.");
        }

        Appliance appliance = new Appliance();
        appliance.setBrand(dto.getBrand());
        appliance.setModelNumber(dto.getModelNumber());
        appliance.setSerialNumber(dto.getSerialNumber());
        appliance.setPurchaseDate(dto.getPurchaseDate());
        appliance.setInvoiceUrl(dto.getInvoiceUrl());
        appliance.setWarrantyExpiryDate(dto.getWarrantyExpiryDate());
        appliance.setHomeowner(homeowner);

        Appliance savedAppliance = applianceRepository.save(appliance);
        System.out.println("Appliance saved with ID: " + savedAppliance.getId());
        System.out.println("Appliance serial number: " + savedAppliance.getSerialNumber());
        System.out.println("Saved appliance homeowner ID: " + savedAppliance.getHomeowner().getId());
    }

    public List<ApplianceResponseDto> getHomeownerAppliances(String email) {
        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));

        return applianceRepository.findByHomeowner_Id(homeowner.getId())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ApplianceResponseDto convertToDto(Appliance appliance) {
        ApplianceResponseDto dto = new ApplianceResponseDto();
        dto.setId(appliance.getId());
        dto.setBrand(appliance.getBrand());
        dto.setModelNumber(appliance.getModelNumber());
        dto.setSerialNumber(appliance.getSerialNumber());
        dto.setPurchaseDate(appliance.getPurchaseDate());
        dto.setInvoiceUrl(appliance.getInvoiceUrl());
        dto.setWarrantyExpiryDate(appliance.getWarrantyExpiryDate());
        dto.setHomeownerName(appliance.getHomeowner().getFirstName() + " " + appliance.getHomeowner().getLastName());
        return dto;
    }
}
