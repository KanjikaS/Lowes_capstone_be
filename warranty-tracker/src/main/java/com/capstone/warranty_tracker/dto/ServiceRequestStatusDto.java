package com.capstone.warranty_tracker.dto;

import com.capstone.warranty_tracker.model.ServiceStatus;
import lombok.Data;

@Data
public class ServiceRequestStatusDto {
    private ServiceStatus status;
}
