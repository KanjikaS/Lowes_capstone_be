
package com.capstone.warranty_tracker.dto;

import com.capstone.warranty_tracker.model.ServiceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestResponseDto {
    private Long id;
    private String issueDescription;
    private LocalDateTime preferredSlot;
    private ServiceStatus status;
    private LocalDateTime createdAt;
    private String homeownerName;   // Derived from Homeowner entity
    private String applianceInfo;   // Concatenation of brand, modelNumber, serialNumber
}
