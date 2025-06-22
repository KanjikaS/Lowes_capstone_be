package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.ServiceRequestDto;
import com.capstone.warranty_tracker.model.Appliance;
import com.capstone.warranty_tracker.model.Homeowner;
import com.capstone.warranty_tracker.model.ServiceRequest;
import com.capstone.warranty_tracker.model.ServiceStatus;
import com.capstone.warranty_tracker.repository.ApplianceRepository;
import com.capstone.warranty_tracker.repository.HomeownerRepository;
import com.capstone.warranty_tracker.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;

@Service
public class ServiceRequestService {

    @Autowired
    private HomeownerRepository homeownerRepository;

    @Autowired
    private ApplianceRepository applianceRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    public void createRequest(ServiceRequestDto dto, String email) throws AccessDeniedException {

    }
}
