package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.*;
import com.capstone.warranty_tracker.model.User;
import com.capstone.warranty_tracker.repository.UserRepository;
import com.capstone.warranty_tracker.service.ApplianceService;
import com.capstone.warranty_tracker.service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/homeowner")
@PreAuthorize("hasRole('HOMEOWNER')")
public class HomeownerController {

    @Autowired
    private ApplianceService applianceService;

    @Autowired
    private ServiceRequestService serviceRequestService;

    @Autowired
    private UserRepository userRepository;

    //  api without invoice
//    @PostMapping("/appliance")
//    public ResponseEntity<String> addAppliance(@RequestBody ApplianceRequestDto dto, Principal principal) {
//        applianceService.addAppliance(dto, principal.getName());
//        return ResponseEntity.ok("Appliance registered successfully.");
//    }

    @PostMapping(value = "/appliance", consumes = {"multipart/form-data"})
    public ResponseEntity<String> addAppliance(
            @RequestPart("appliance") ApplianceRequestDto dto,
            @RequestPart("invoice") MultipartFile invoiceFile,
            Principal principal) throws IOException {

        applianceService.addApplianceWithInvoice(dto, invoiceFile, principal.getName());
        return ResponseEntity.ok("Appliance registered successfully with invoice.");
    }

    @PostMapping("/service-request")
    public ResponseEntity<?> createServiceRequest(@RequestBody ServiceRequestDto dto, Principal principal) {
        return ResponseEntity.ok(serviceRequestService.createRequest(dto, principal.getName()));
    }

    @GetMapping("/service-requests")
    public ResponseEntity<?> getAllServiceRequests(Principal principal) {
        return ResponseEntity.ok(serviceRequestService.getHomeownerRequests(principal.getName()));
    }

    @GetMapping("/service-request/{id}")
    public ResponseEntity<?> getServiceRequestById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(serviceRequestService.getRequestById(id, principal.getName()));
    }

    @PutMapping("/service-request/{id}")
    public ResponseEntity<?> updateServiceRequest(@PathVariable Long id, @RequestBody ServiceRequestDto dto, Principal principal) {
        return ResponseEntity.ok(serviceRequestService.updateRequest(id, dto, principal.getName()));
    }

    @DeleteMapping("/service-request/{id}")
    public ResponseEntity<?> cancelServiceRequest(@PathVariable Long id, Principal principal) {
        serviceRequestService.cancelRequest(id, principal.getName());
        return ResponseEntity.ok("Service request cancelled.");
    }

    @GetMapping("/appliances")
    public ResponseEntity<?> getAllAppliances(Principal principal) {
        return ResponseEntity.ok(applianceService.getHomeownerAppliances(principal.getName()));
    }

// without invoice
//    @PutMapping("/edit/{id}")
//    public ResponseEntity<ApplianceResponseDto> updateAppliance(
//            @PathVariable Long id,
//            @RequestBody ApplianceRequestDto dto,
//            Principal principal) {
//        ApplianceResponseDto updated = applianceService.updateAppliance(id, dto, principal.getName());
//        return ResponseEntity.ok(updated);
//    }

    //with invoice
    @PutMapping(value = "/edit/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateAppliance(
            @PathVariable Long id,
            @RequestPart("appliance") ApplianceRequestDto dto,
            @RequestPart(value = "invoice", required = false) MultipartFile invoiceFile,
            Principal principal) throws IOException {

        applianceService.updateApplianceWithInvoice(id, dto, invoiceFile, principal.getName());
        return ResponseEntity.ok("Appliance updated successfully");
    }

    @GetMapping("/appliance/serial/{serialNumber}")
    public ResponseEntity<ApplianceResponseDto> getApplianceBySerialNumber(@PathVariable String serialNumber, Principal principal) {
        ApplianceResponseDto dto = applianceService.getBySerialNumber(serialNumber, principal.getName());
        return ResponseEntity.ok(dto);
    }


    // Delete appliance
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAppliance(
            @PathVariable Long id,
            Principal principal) {
        applianceService.deleteAppliance(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/service-history/appliance/{applianceId}")
    public ResponseEntity<List<ServiceHistoryDto>> getServiceHistoryByAppliance(@PathVariable Long applianceId) {
        List<ServiceHistoryDto> history = serviceRequestService.getServiceHistoryByAppliance(applianceId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/service-history/{username}")
    public ResponseEntity<List<ServiceHistoryDto>> getServiceHistoryForLoggedInUser(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String emailFromJwt = userDetails.getUsername(); // this is the email from JWT

        User loggedInUser = userRepository.findByEmail(emailFromJwt)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!loggedInUser.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ServiceHistoryDto> history = serviceRequestService.getServiceHistoryByUsername(username);
        return ResponseEntity.ok(history);
    }

}
