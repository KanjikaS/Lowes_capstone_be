package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.RescheduleDTO;
import com.capstone.warranty_tracker.dto.ServiceRequestDto;
import com.capstone.warranty_tracker.model.*;
import com.capstone.warranty_tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

@Service
public class ServiceRequestService {

    @Autowired
    private HomeownerRepository homeownerRepository;

    @Autowired
    private ApplianceRepository applianceRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TechnicianRepository technicianRepository;


    public void createRequest(ServiceRequestDto dto, String email) throws AccessDeniedException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!(user instanceof Homeowner homeowner)) {
            throw new AccessDeniedException("Only homeowners can create requests");
        }

        Appliance appliance = applianceRepository.findById(dto.getApplianceId())
                .orElseThrow(() -> new RuntimeException("Appliance not found"));

        Technician technician = technicianRepository.findById(dto.getTechnicianId())
                .orElseThrow(() -> new RuntimeException("Technician not found"));

        if (serviceRequestRepository.existsByTechnician_IdAndScheduledSlot(dto.getTechnicianId(), dto.getPreferredSlot())) {
            throw new RuntimeException("Slot already booked for technician");
        }

        ServiceRequest request = new ServiceRequest();
        request.setDescription(dto.getIssueDescription());
        request.setScheduledSlot(dto.getPreferredSlot());
        request.setAppliance(appliance);
        request.setHomeowner(homeowner);
        request.setTechnician(technician);
        request.setStatus(ServiceStatus.REQUESTED);
        request.setDeleted(false);

        serviceRequestRepository.save(request);
    }

    public void rescheduleAppointment(Long requestId, RescheduleDTO dto) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.isDeleted()) {
            throw new RuntimeException("Cannot reschedule a deleted request");
        }

        if (serviceRequestRepository.existsByTechnician_IdAndScheduledSlot(
                request.getTechnician().getId(), dto.scheduledSlot())) {
            throw new RuntimeException("Slot already taken");
        }

        request.setScheduledSlot(dto.scheduledSlot());
        serviceRequestRepository.save(request);
    }

    public void cancelAppointment(Long requestId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setDeleted(true);
        request.setStatus(ServiceStatus.CANCELLED);
        serviceRequestRepository.save(request);
    }
}

