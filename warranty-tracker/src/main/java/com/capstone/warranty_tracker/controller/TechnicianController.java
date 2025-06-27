package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.ServiceRequestResponseDto;
import com.capstone.warranty_tracker.dto.TechnicianResponseDto;
import com.capstone.warranty_tracker.model.Technician;
import com.capstone.warranty_tracker.repository.TechnicianRepository;
import com.capstone.warranty_tracker.service.TechnicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.capstone.warranty_tracker.dto.UpdateRequestStatusDto;
import com.capstone.warranty_tracker.security.JwtUtil;
import com.capstone.warranty_tracker.dto.ServiceHistoryDto;

import java.util.List;

@RestController
@RequestMapping("/technician")
@PreAuthorize("hasRole('TECHNICIAN')")
public class TechnicianController {

    @Autowired
    private TechnicianService technicianService;

    @Autowired
    private TechnicianRepository technicianRepository;

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

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ GET in-progress requests
    @GetMapping("/requests/in-progress")
    public ResponseEntity<List<ServiceRequestResponseDto>> getInProgressRequests(
            @RequestHeader("Authorization") String authHeader) {

        // Remove "Bearer " prefix and extract email from JWT
        String token = authHeader.substring(7);
        String technicianEmail = jwtUtil.extractEmail(token);

        // Call service method
        List<ServiceRequestResponseDto> requests = technicianService.getInProgressRequestsForTechnician(technicianEmail);

        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/completed")
    public ResponseEntity<List<ServiceRequestResponseDto>> getCompletedRequests(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.substring(7));
        List<ServiceRequestResponseDto> completed = technicianService.getCompletedRequestsForTechnician(email);
        return ResponseEntity.ok(completed);
    }

    @GetMapping("/profile")
    public ResponseEntity<TechnicianResponseDto> getTechnicianProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        TechnicianResponseDto profile = technicianService.getTechnicianProfile(email);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/service-history/{technicianId}")
    public ResponseEntity<List<ServiceHistoryDto>> getServiceHistory(
            @PathVariable Long technicianId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // From token
        String emailFromJwt = userDetails.getUsername(); // username is email by default

        Technician loggedInTechnician = technicianRepository.findByEmail(emailFromJwt)
                .orElseThrow(() -> new RuntimeException("Technician not found"));

        if (!loggedInTechnician.getId().equals(technicianId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // or a custom message
        }

        List<ServiceHistoryDto> history = technicianService.getServiceHistoryByTechnician_Id(technicianId);
        return ResponseEntity.ok(history);
    }

}
