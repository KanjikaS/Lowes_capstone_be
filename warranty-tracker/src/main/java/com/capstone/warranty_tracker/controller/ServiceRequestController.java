package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.RescheduleDTO;
import com.capstone.warranty_tracker.dto.ServiceRequestDto;
import com.capstone.warranty_tracker.service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/request")
public class ServiceRequestController {

    @Autowired
    private ServiceRequestService serviceRequestService;

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody ServiceRequestDto dto, Principal principal)
    {
        serviceRequestService.createRequest(dto, principal.getName());
        return ResponseEntity.ok("Request created");
    }

    @PutMapping("/reschedule/{id}")
    public ResponseEntity<String> reschedule(@PathVariable Long id, @RequestBody RescheduleDTO dto) {
        serviceRequestService.rescheduleAppointment(id, dto);
        return ResponseEntity.ok("Request rescheduled");
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<String> cancel(@PathVariable Long id) {
        serviceRequestService.cancelAppointment(id);
        return ResponseEntity.ok("Request cancelled");
    }
}
