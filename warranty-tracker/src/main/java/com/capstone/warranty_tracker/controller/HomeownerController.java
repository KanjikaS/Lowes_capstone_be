package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.ApplianceRequestDto;
import com.capstone.warranty_tracker.dto.ServiceRequestDto;
import com.capstone.warranty_tracker.service.ApplianceService;
import com.capstone.warranty_tracker.service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/homeowner")
@PreAuthorize("hasRole('HOMEOWNER')")
public class HomeownerController {

    @Autowired
    private ApplianceService applianceService;

    @Autowired
    private ServiceRequestService serviceRequestService;

    @PostMapping("/appliance")
    public ResponseEntity<String> addAppliance(@RequestBody ApplianceRequestDto dto, Principal principal) {
        applianceService.addAppliance(dto, principal.getName());
        return ResponseEntity.ok("Appliance registered successfully.");
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
}
