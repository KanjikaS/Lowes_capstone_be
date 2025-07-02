package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.*;
import com.capstone.warranty_tracker.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService service;

    @PostMapping
    public void create(@RequestBody ServiceRequestDto d, Principal p) {
        service.createRequest(d, p.getName());
    }

    @PutMapping("/{id}/reschedule")
    public void reschedule(@PathVariable Long id, @RequestBody ServiceRequestUpdateDto d, Principal p) {
        service.reschedule(id, d.getNewSlot(), p.getName());
    }

    @PatchMapping("/{id}/status")
    public void status(@PathVariable Long id, @RequestBody ServiceRequestStatusDto d, Principal p) {
        service.updateStatus(id, d.getStatus(), p.getName());
    }

    @DeleteMapping("/{id}")
    public void cancel(@PathVariable Long id, Principal p) {
        service.cancelRequest(id, p.getName());
    }

    @GetMapping("/slots")
    public List<LocalDateTime> slots(@RequestParam Long technicianId, @RequestParam LocalDate day) {
        return service.availableSlots(technicianId, day);
    }
}