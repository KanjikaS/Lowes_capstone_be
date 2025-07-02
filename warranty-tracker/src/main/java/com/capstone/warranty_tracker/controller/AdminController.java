package com.capstone.warranty_tracker.controller;
//import com.capstone.warranty_tracker.dto.TechnicianAssignmentWrapper;
import com.capstone.warranty_tracker.dto.TechnicianResponseDto;
import com.capstone.warranty_tracker.dto.ServiceRequestDto;
import com.capstone.warranty_tracker.service.AdminService;
import com.capstone.warranty_tracker.service.ServiceRequestService;
import com.capstone.warranty_tracker.service.TechnicianService;
import com.capstone.warranty_tracker.service.ApplianceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
 // Required if frontend is on different port


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
    public ResponseEntity<?> getStats(){
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/recent-service-requests")
    public ResponseEntity<?> getRecentServiceRequests(){
        return ResponseEntity.ok(ResponseEntity.ok(adminService.getRecentServiceRequest()));
    }

    @GetMapping("/all-technicians")
    public ResponseEntity<?> getAllTechnicians() {
        return ResponseEntity.ok(technicianService.getAllTechnicians());
    }

  @GetMapping("/available-technicians")
    public ResponseEntity <?> getAvailableTechnicians(){
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
    public ResponseEntity<?> getAllServiceRequests(){
        return ResponseEntity.ok(ResponseEntity.ok(adminService.getAllServiceRequests()));
    }

    @GetMapping("/all-appliances")
    public ResponseEntity<?>getAllAppliances(){
        return ResponseEntity.ok(adminService.getAllAppliances());
    }
}

