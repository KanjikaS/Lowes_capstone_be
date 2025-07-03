package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.ServiceRequestAdminDto;
import com.capstone.warranty_tracker.dto.ServiceRequestResponseDto;
import com.capstone.warranty_tracker.dto.ServiceHistoryDto;
import com.capstone.warranty_tracker.service.AdminService;
import com.capstone.warranty_tracker.service.ServiceRequestService;
import com.capstone.warranty_tracker.service.TechnicianService;
import com.capstone.warranty_tracker.service.ApplianceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private TechnicianService technicianService;

    @Autowired
    private ServiceRequestService serviceRequestService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private ApplianceService applianceService;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/recent-service-requests")
    public ResponseEntity<?> getRecentServiceRequests() {
        return ResponseEntity.ok(adminService.getRecentServiceRequest());
    }

    @GetMapping("/all-technicians")
    public ResponseEntity<?> getAllTechnicians() {
        return ResponseEntity.ok(technicianService.getAllTechnicians());
    }

    @GetMapping("/available-technicians")
    public ResponseEntity<?> getAvailableTechnicians() {
        return ResponseEntity.ok(technicianService.getAvailableTechnicians());
    }

    @PostMapping("/assign-technician")
    public ResponseEntity<String> assignTechnicianToRequest(
            @RequestParam Long technicianId,
            @RequestParam Long requestId) {
        System.out.println("before entering assigned");
        boolean assigned = adminService.assignTechnicianToRequest(technicianId, requestId);

        if (assigned) {
            return ResponseEntity.ok("Technician successfully assigned to service request.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Assignment failed. Check IDs or availability.");
        }
    }

    @GetMapping("/all-service-requests")
    public ResponseEntity<List<ServiceRequestAdminDto>> getAllServiceRequests() {
        return ResponseEntity.ok(adminService.getAllServiceRequests());
    }

    @GetMapping("/all-appliances")
    public ResponseEntity<?> getAllAppliances() {
        return ResponseEntity.ok(adminService.getAllAppliances());
    }

    @GetMapping("/service-history/appliance/{applianceId}")
    public ResponseEntity<List<ServiceHistoryDto>> getServiceHistoryByAppliance(@PathVariable Long applianceId) {
        List<ServiceHistoryDto> history = serviceRequestService.getServiceHistoryByAppliance(applianceId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/service-history/technician/{technicianId}")
    public ResponseEntity<List<ServiceHistoryDto>> getServiceHistoryByTechnician_Id(@PathVariable Long technicianId) {
        List<ServiceHistoryDto> history = serviceRequestService.getServiceHistoryByTechnician_Id(technicianId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/service-history/homeowner/{username}")
    public ResponseEntity<List<ServiceHistoryDto>> getServiceHistoryByHomeowner(@PathVariable String username) {
        List<ServiceHistoryDto> history = serviceRequestService.getServiceHistoryByUsername(username);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/technician-assigned-requests")
    public ResponseEntity<List<ServiceRequestResponseDto>> getAssignedRequests(@RequestParam Long technicianId) {
        return ResponseEntity.ok(technicianService.getAssignedRequestsForTechnicianByID(technicianId));
    }

    @GetMapping("/technician/in-progress")
    public ResponseEntity<List<ServiceRequestResponseDto>> getInProgressRequests(@RequestParam String technicianEmail) {
        List<ServiceRequestResponseDto> requests = technicianService.getInProgressRequestsForTechnician(technicianEmail);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/technician/completed")
    public ResponseEntity<List<ServiceRequestResponseDto>> getCompletedRequests(@RequestParam String technicianEmail) {
        List<ServiceRequestResponseDto> completed = technicianService.getCompletedRequestsForTechnician(technicianEmail);
        return ResponseEntity.ok(completed);
    }
}

