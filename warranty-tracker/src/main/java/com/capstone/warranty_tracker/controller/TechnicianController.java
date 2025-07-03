package com.capstone.warranty_tracker.controller;
import com.capstone.warranty_tracker.dto.*;

import com.capstone.warranty_tracker.model.Technician;
import com.capstone.warranty_tracker.repository.TechnicianRepository;
import com.capstone.warranty_tracker.service.TechnicianService;
import com.capstone.warranty_tracker.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/technician")
@PreAuthorize("hasRole('TECHNICIAN')")
public class TechnicianController {

    @Autowired
    private TechnicianService technicianService;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/assigned-requests")
    public ResponseEntity<List<ServiceRequestResponseDto>> getAssignedRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
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

    @GetMapping("/requests/in-progress")
    public ResponseEntity<List<ServiceRequestResponseDto>> getInProgressRequests(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String technicianEmail = jwtUtil.extractEmail(token);
        List<ServiceRequestResponseDto> requests = technicianService.getInProgressRequestsForTechnician(technicianEmail);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/completed")
    public ResponseEntity<List<ServiceRequestResponseDto>> getCompletedRequests(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String technicianEmail = jwtUtil.extractEmail(token);
        List<ServiceRequestResponseDto> completed = technicianService.getCompletedRequestsForTechnician(technicianEmail);
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
        String emailFromJwt = userDetails.getUsername();
        Technician loggedInTechnician = technicianRepository.findByEmail(emailFromJwt)
                .orElseThrow(() -> new RuntimeException("Technician not found"));
        if (!loggedInTechnician.getId().equals(technicianId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ServiceHistoryDto> history = technicianService.getServiceHistoryByTechnician_Id(technicianId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/stats")
    public ResponseEntity<TechnicianStatsDto> getTechnicianStats(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        TechnicianStatsDto stats = technicianService.getTechnicianStats(email);
        return ResponseEntity.ok(stats);
    }


    @PostMapping("/service-request/{requestId}/completion")
    public ResponseEntity<String> submitCompletionForm(
            @PathVariable Long requestId,
            @RequestBody CompletionFormDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        technicianService.saveCompletionForm(requestId, dto, userDetails.getUsername());
        return ResponseEntity.ok("Completion form submitted successfully.");
    }



    @GetMapping("/service-request/{requestId}/completion")
    public ResponseEntity<CompletionFormResponseDto> getCompletionForm(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CompletionFormResponseDto dto = technicianService.getCompletionForm(requestId, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }







}
