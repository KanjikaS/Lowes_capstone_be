package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.*;
import com.capstone.warranty_tracker.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/technician/appointments")
@RequiredArgsConstructor
public class TechnicianAppointmentController {

    private final ServiceRequestService service;

    @PatchMapping("/{id}/status")
    public void update(@PathVariable Long id,@RequestBody ServiceRequestStatusDto d,Principal p){
        service.technicianUpdateStatus(id,d.getStatus(),p.getName());
    }

    @PutMapping("/{id}/reschedule")
    public void reschedule(@PathVariable Long id,@RequestBody ServiceRequestTechRescheduleDto d,Principal p){
        service.technicianReschedule(id,d.getNewSlot(),p.getName());
    }
}