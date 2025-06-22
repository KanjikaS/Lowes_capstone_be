package com.capstone.warranty_tracker.model;

public enum ServiceStatus {
    REQUESTED,       // Raised by Homeowner
    ASSIGNED,        // Assigned to Technician by Admin
    IN_PROGRESS,     // Technician has started the work
    COMPLETED,       // Technician marked the work as done
    CANCELLED,       // Cancelled by Homeowner/Admin
    RESCHEDULED      // If Homeowner changes time
}