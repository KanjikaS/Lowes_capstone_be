package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.ServiceRequestResponseDto;
import com.capstone.warranty_tracker.service.TechnicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.capstone.warranty_tracker.dto.UpdateRequestStatusDto;


import java.util.List;

@RestController
@RequestMapping("/technician")
@PreAuthorize("hasRole('TECHNICIAN')")
public class TechnicianController {

    @Autowired
    private TechnicianService technicianService;

    @GetMapping("/assigned-requests")
    public ResponseEntity<List<ServiceRequestResponseDto>> getAssignedRequests(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<ServiceRequestResponseDto> assignedRequests = technicianService.getAssignedRequestsForTechnician(email);
        return ResponseEntity.ok(assignedRequests);
    }

    @PutMapping("/update-status")
    public ResponseEntity<String> updateServiceRequestStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateRequestStatusDto updateDto
    ) {
        String technicianEmail = userDetails.getUsername();
        technicianService.updateRequestStatus(technicianEmail, updateDto);
        return ResponseEntity.ok("Service request status updated successfully.");
    }



}
