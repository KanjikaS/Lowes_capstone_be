package com.capstone.warranty_tracker.controller;

import com.capstone.warranty_tracker.dto.AdminScheduleDto;
import com.capstone.warranty_tracker.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/appointments")
@RequiredArgsConstructor
public class AdminAppointmentController {

    private final ServiceRequestService service;

    @PatchMapping("/{id}/approve")
    public void approve(@PathVariable Long id) {
        service.adminApprove(id);
    }

    @PostMapping
    public void create(@RequestBody AdminScheduleDto d) {
        service.adminCreate(d);
    }

}