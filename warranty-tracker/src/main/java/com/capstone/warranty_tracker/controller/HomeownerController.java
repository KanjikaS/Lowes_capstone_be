package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.ApplianceRequestDto;
import com.capstone.warranty_tracker.dto.ServiceRequestDto;
import com.capstone.warranty_tracker.service.ApplianceService;
import com.capstone.warranty_tracker.service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> createServiceRequest(@RequestBody ServiceRequestDto dto, Principal principal) {
        serviceRequestService.createRequest(dto, principal.getName());
        return ResponseEntity.ok("Service request submitted.");
    }
}
