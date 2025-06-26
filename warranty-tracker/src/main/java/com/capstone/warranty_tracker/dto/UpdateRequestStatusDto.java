package com.capstone.warranty_tracker.dto;

import com.capstone.warranty_tracker.model.ServiceStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRequestStatusDto {
    private Long requestId;
    private ServiceStatus status;
}
