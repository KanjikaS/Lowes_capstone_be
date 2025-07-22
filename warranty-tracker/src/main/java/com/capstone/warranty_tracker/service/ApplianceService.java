//package com.capstone.warranty_tracker.service;
//
//import com.capstone.warranty_tracker.dto.ApplianceRequestDto;
//import com.capstone.warranty_tracker.dto.ApplianceResponseDto;
//import com.capstone.warranty_tracker.model.Appliance;
//import com.capstone.warranty_tracker.model.Homeowner;
//import com.capstone.warranty_tracker.repository.ApplianceRepository;
//import com.capstone.warranty_tracker.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import com.capstone.warranty_tracker.repository.HomeownerRepository;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class ApplianceService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ApplianceRepository applianceRepository;
//
//    @Autowired
//    private HomeownerRepository homeownerRepository;
//
//    // without invoice
////    public void addAppliance(ApplianceRequestDto dto, String email) {
////        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
////                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));
////
////        System.out.println("Adding appliance for homeowner: " + homeowner.getFirstName() + " " + homeowner.getLastName());
////        System.out.println("Homeowner ID: " + homeowner.getId());
////
////        // Check if serial number already exists
////        if (applianceRepository.existsBySerialNumber(dto.getSerialNumber())) {
////            throw new IllegalArgumentException("Appliance with serial number '" + dto.getSerialNumber() + "' already exists. Serial numbers must be unique.");
////        }
////
////        Appliance appliance = new Appliance();
////        appliance.setBrand(dto.getBrand());
////        appliance.setModelNumber(dto.getModelNumber());
////        appliance.setSerialNumber(dto.getSerialNumber());
////        appliance.setPurchaseDate(dto.getPurchaseDate());
////        appliance.setInvoiceUrl(dto.getInvoiceUrl());
////        appliance.setWarrantyExpiryDate(dto.getWarrantyExpiryDate());
////        appliance.setHomeowner(homeowner);
////
////        Appliance savedAppliance = applianceRepository.save(appliance);
////        System.out.println("Appliance saved with ID: " + savedAppliance.getId());
////        System.out.println("Appliance serial number: " + savedAppliance.getSerialNumber());
////        System.out.println("Saved appliance homeowner ID: " + savedAppliance.getHomeowner().getId());
////    }
//
//    public List<ApplianceResponseDto> getHomeownerAppliances(String email) {
//        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));
//
//        return applianceRepository.findByHomeowner_Id(homeowner.getId())
//                .stream()
//                .map(this::convertToDto)
//                .collect(Collectors.toList());
//    }
//
//    private ApplianceResponseDto convertToDto(Appliance appliance) {
//        ApplianceResponseDto dto = new ApplianceResponseDto();
//        dto.setId(appliance.getId());
//        dto.setBrand(appliance.getBrand());
//         dto.setCategory(appliance.getCategory());
//        dto.setModelNumber(appliance.getModelNumber());
//        dto.setSerialNumber(appliance.getSerialNumber());
//        dto.setPurchaseDate(appliance.getPurchaseDate());
//        dto.setInvoiceUrl(appliance.getInvoiceUrl());
//        dto.setWarrantyExpiryDate(appliance.getWarrantyExpiryDate());
//        dto.setHomeownerName(appliance.getHomeowner().getFirstName() + " " + appliance.getHomeowner().getLastName());
//        return dto;
//    }
//
//    public void deleteAppliance(Long id, String email) {
//        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));
//
//        Appliance appliance = applianceRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Appliance not found with ID: " + id));
//
//        // Check ownership
//        if (!appliance.getHomeowner().getId().equals(homeowner.getId())) {
//            throw new SecurityException("You do not have permission to delete this appliance");
//        }
//
//        applianceRepository.delete(appliance);
//    }
//
//// without invoice
////    public ApplianceResponseDto updateAppliance(Long id, ApplianceRequestDto dto, String email) {
////        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
////                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));
////
////        Appliance appliance = applianceRepository.findById(id)
////                .orElseThrow(() -> new IllegalArgumentException("Appliance not found with ID: " + id));
////
////        // Check ownership
////        if (!appliance.getHomeowner().getId().equals(homeowner.getId())) {
////            throw new SecurityException("You do not have permission to update this appliance");
////        }
////
////        // Update fields
////        appliance.setBrand(dto.getBrand());
////        appliance.setModelNumber(dto.getModelNumber());
////        appliance.setSerialNumber(dto.getSerialNumber());
////        appliance.setPurchaseDate(dto.getPurchaseDate());
////        appliance.setInvoiceUrl(dto.getInvoiceUrl());
////        appliance.setWarrantyExpiryDate(dto.getWarrantyExpiryDate());
////
////        Appliance updatedAppliance = applianceRepository.save(appliance);
////        return convertToDto(updatedAppliance);
////    }
//
//    //with invoice
//    public ApplianceResponseDto updateApplianceWithInvoice(Long id, ApplianceRequestDto dto, MultipartFile invoiceFile, String email) throws IOException {
//        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));
//
//        Appliance appliance = applianceRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Appliance not found with ID: " + id));
//
//        // Check ownership
//        if (!appliance.getHomeowner().getId().equals(homeowner.getId())) {
//            throw new SecurityException("You do not have permission to update this appliance");
//        }
//
//        // Update basic fields
//        appliance.setBrand(dto.getBrand());
//         appliance.setCategory(dto.getCategory());
//        appliance.setModelNumber(dto.getModelNumber());
//        appliance.setSerialNumber(dto.getSerialNumber());
//        appliance.setPurchaseDate(dto.getPurchaseDate());
//        appliance.setWarrantyExpiryDate(dto.getWarrantyExpiryDate());
//
//        // If a new invoice file is uploaded, handle saving and update URL
//        if (invoiceFile != null && !invoiceFile.isEmpty()) {
//            String uploadDir = "uploads/invoices/";
//            Path uploadPath = Paths.get(uploadDir);
//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//            }
//
//            String originalFilename = invoiceFile.getOriginalFilename();
//            String fileName = dto.getSerialNumber() + "_" + (originalFilename != null ? originalFilename : "invoice.pdf");
//            Path filePath = uploadPath.resolve(fileName);
//            Files.copy(invoiceFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//            appliance.setInvoiceUrl(uploadDir + fileName);
//            System.out.println("Updated invoice at: " + appliance.getInvoiceUrl());
//        }
//
//        Appliance updated = applianceRepository.save(appliance);
//        return convertToDto(updated);
//    }
//
//
//
//    public void addApplianceWithInvoice(ApplianceRequestDto dto, MultipartFile invoiceFile, String email) throws IOException {
//        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));
//
//        // Check if serial number already exists
//        if (applianceRepository.existsBySerialNumber(dto.getSerialNumber())) {
//            throw new IllegalArgumentException("Appliance with serial number '" + dto.getSerialNumber() + "' already exists.");
//        }
//
//        // Define where to save the invoice PDF on your server
//        String uploadDir = "uploads/invoices/";
//        // Make sure the directory exists
//        Path uploadPath = Paths.get(uploadDir);
//        if (!Files.exists(uploadPath)) {
//            Files.createDirectories(uploadPath);
//        }
//
//        // Generate a unique file name for the invoice (e.g. serialNumber + original filename)
//        String originalFilename = invoiceFile.getOriginalFilename();
//        String fileName = dto.getSerialNumber() + "_" + (originalFilename != null ? originalFilename : "invoice.pdf");
//
//        // Path to save file
//        Path filePath = uploadPath.resolve(fileName);
//
//        // Save the file locally
//        Files.copy(invoiceFile.getInputStream(), filePath);
//
//        Appliance appliance = new Appliance();
//        appliance.setBrand(dto.getBrand());
//         appliance.setCategory(dto.getCategory());
//        appliance.setModelNumber(dto.getModelNumber());
//        appliance.setSerialNumber(dto.getSerialNumber());
//        appliance.setPurchaseDate(dto.getPurchaseDate());
//
//        // Save relative or absolute URL/path of the invoice file
//        appliance.setInvoiceUrl( uploadDir + fileName);
//
//        appliance.setWarrantyExpiryDate(dto.getWarrantyExpiryDate());
//        appliance.setHomeowner(homeowner);
//
//        Appliance savedAppliance = applianceRepository.save(appliance);
//        System.out.println(filePath.toAbsolutePath());
//        System.out.println("Appliance saved with ID: " + savedAppliance.getId());
//        System.out.println("Invoice saved at: " + appliance.getInvoiceUrl());
//
//    }
//
//
//    public ApplianceResponseDto getBySerialNumber(String serialNumber, String email) {
//        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found"));
//
//        Appliance appliance = applianceRepository.findBySerialNumber(serialNumber)
//                .orElseThrow(() -> new IllegalArgumentException("Appliance not found"));
//
//
//        return convertToDto(appliance);
//    }
//
//}

package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.ApplianceRequestDto;
import com.capstone.warranty_tracker.dto.ApplianceResponseDto;
import com.capstone.warranty_tracker.model.Appliance;
import com.capstone.warranty_tracker.model.Homeowner;
import com.capstone.warranty_tracker.repository.ApplianceRepository;
import com.capstone.warranty_tracker.repository.UserRepository;
import com.capstone.warranty_tracker.repository.HomeownerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplianceService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplianceRepository applianceRepository;

    @Autowired
    private HomeownerRepository homeownerRepository;

    /**
     * Get all appliances for a homeowner.
     */
    public List<ApplianceResponseDto> getHomeownerAppliances(String email) {
        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));

        return applianceRepository.findByHomeowner_Id(homeowner.getId())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Delete an appliance by ID.
     */
    public void deleteAppliance(Long id, String email) {
        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));

        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appliance not found with ID: " + id));

        if (!appliance.getHomeowner().getId().equals(homeowner.getId())) {
            throw new SecurityException("You do not have permission to delete this appliance");
        }

        applianceRepository.delete(appliance);
    }

    /**
     * Add new appliance with optional invoice.
     */
    public void addApplianceWithInvoice(ApplianceRequestDto dto, MultipartFile invoiceFile, String email) throws IOException {
        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));

        if (applianceRepository.existsBySerialNumber(dto.getSerialNumber())) {
            throw new IllegalArgumentException("Appliance with serial number '" + dto.getSerialNumber() + "' already exists.");
        }

        Appliance appliance = new Appliance();
        appliance.setBrand(dto.getBrand());
        appliance.setCategory(dto.getCategory());
        appliance.setModelNumber(dto.getModelNumber());
        appliance.setSerialNumber(dto.getSerialNumber());
        appliance.setPurchaseDate(dto.getPurchaseDate());
        appliance.setWarrantyExpiryDate(dto.getWarrantyExpiryDate());
        appliance.setHomeowner(homeowner);

        // If invoice is provided, save it
        if (invoiceFile != null && !invoiceFile.isEmpty()) {
            String uploadDir = "uploads/invoices/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = invoiceFile.getOriginalFilename();
            String fileName = dto.getSerialNumber() + "_" + (originalFilename != null ? originalFilename : "invoice.pdf");
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(invoiceFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            appliance.setInvoiceUrl(uploadDir + fileName);
            System.out.println("Saved invoice at: " + filePath.toAbsolutePath());
        } else {
            appliance.setInvoiceUrl(null);
            System.out.println("No invoice uploaded for appliance: " + dto.getSerialNumber());
        }

        Appliance savedAppliance = applianceRepository.save(appliance);
        System.out.println("Appliance saved with ID: " + savedAppliance.getId());
    }

    /**
     * Update an existing appliance with optional new invoice.
     */
    public ApplianceResponseDto updateApplianceWithInvoice(Long id, ApplianceRequestDto dto, MultipartFile invoiceFile, String email) throws IOException {
        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));

        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appliance not found with ID: " + id));

        if (!appliance.getHomeowner().getId().equals(homeowner.getId())) {
            throw new SecurityException("You do not have permission to update this appliance");
        }

        appliance.setBrand(dto.getBrand());
        appliance.setCategory(dto.getCategory());
        appliance.setModelNumber(dto.getModelNumber());
        appliance.setSerialNumber(dto.getSerialNumber());
        appliance.setPurchaseDate(dto.getPurchaseDate());
        appliance.setWarrantyExpiryDate(dto.getWarrantyExpiryDate());

        if (invoiceFile != null && !invoiceFile.isEmpty()) {
            String uploadDir = "uploads/invoices/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = invoiceFile.getOriginalFilename();
            String fileName = dto.getSerialNumber() + "_" + (originalFilename != null ? originalFilename : "invoice.pdf");
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(invoiceFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            appliance.setInvoiceUrl(uploadDir + fileName);
            System.out.println("Updated invoice at: " + filePath.toAbsolutePath());
        }

        Appliance updated = applianceRepository.save(appliance);
        return convertToDto(updated);
    }

    /**
     * Get an appliance by serial number.
     */
    public ApplianceResponseDto getBySerialNumber(String serialNumber, String email) {
        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found"));

        Appliance appliance = applianceRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("Appliance not found"));

        return convertToDto(appliance);
    }

    /**
     * Utility to convert to DTO.
     */
    private ApplianceResponseDto convertToDto(Appliance appliance) {
        ApplianceResponseDto dto = new ApplianceResponseDto();
        dto.setId(appliance.getId());
        dto.setBrand(appliance.getBrand());
        dto.setCategory(appliance.getCategory());
        dto.setModelNumber(appliance.getModelNumber());
        dto.setSerialNumber(appliance.getSerialNumber());
        dto.setPurchaseDate(appliance.getPurchaseDate());
        dto.setInvoiceUrl(appliance.getInvoiceUrl());
        dto.setWarrantyExpiryDate(appliance.getWarrantyExpiryDate());
        dto.setHomeownerName(appliance.getHomeowner().getFirstName() + " " + appliance.getHomeowner().getLastName());
        return dto;
    }

}

