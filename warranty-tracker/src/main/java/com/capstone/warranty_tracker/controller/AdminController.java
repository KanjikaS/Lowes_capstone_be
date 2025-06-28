package com.capstone.warranty_tracker.controller;
import com.capstone.warranty_tracker.dto.TechnicianAssignmentWrapper;
import com.capstone.warranty_tracker.dto.TechnicianResponseDto;
import com.capstone.warranty_tracker.dto.ServiceRequestDto;
import com.capstone.warranty_tracker.service.AdminService;
import com.capstone.warranty_tracker.service.ServiceRequestService;
import com.capstone.warranty_tracker.service.TechnicianService;
import com.capstone.warranty_tracker.service.ApplianceService;
import org.springframework.beans.factory.annotation.Autowired;
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
        System.out.println("Yayyyyyyy");
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/all-technicians")
    public ResponseEntity<?> getAllTechnicians() {
        return ResponseEntity.ok(technicianService.getAllTechnicians());
    }


  @GetMapping("/available-technicians")
    public ResponseEntity <?> getAvailableTechnicians(){
        return ResponseEntity.ok(technicianService.getAvailableTechnicians());
    }
    @PostMapping("/assign-technicians")
    public ResponseEntity<TechnicianAssignmentWrapper> assignTechnicians() {
        TechnicianAssignmentWrapper response = adminService.assignTechniciansToUnassignedRequests();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/all-appliances")
    public ResponseEntity<?>getAllAppliances(){
        return ResponseEntity.ok(adminService.getAllAppliances());
    }
}

