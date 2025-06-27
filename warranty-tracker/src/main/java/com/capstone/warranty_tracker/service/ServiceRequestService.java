package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.ServiceRequestDto;
import com.capstone.warranty_tracker.dto.ServiceRequestResponseDto;
import com.capstone.warranty_tracker.model.Appliance;
import com.capstone.warranty_tracker.model.Homeowner;
import com.capstone.warranty_tracker.model.ServiceRequest;
import com.capstone.warranty_tracker.model.ServiceStatus;
import com.capstone.warranty_tracker.repository.ApplianceRepository;
import com.capstone.warranty_tracker.repository.UserRepository;
import com.capstone.warranty_tracker.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServiceRequestService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplianceRepository applianceRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    public ServiceRequestResponseDto createRequest(ServiceRequestDto dto, String email) throws AccessDeniedException {
        try {
            Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));


            Appliance appliance = applianceRepository.findBySerialNumber(dto.getSerialNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Appliance not found with serial number: " + dto.getSerialNumber()));

            System.out.println("Found appliance: " + appliance.getBrand() + " " + appliance.getModelNumber());
            System.out.println("Appliance ID: " + appliance.getId());
            System.out.println("Appliance Serial Number: " + appliance.getSerialNumber());
            System.out.println("Appliance Homeowner ID: " + appliance.getHomeowner().getId());
            System.out.println("Appliance Homeowner Email: " + appliance.getHomeowner().getEmail());
            System.out.println("Appliance Homeowner Name: " + appliance.getHomeowner().getFirstName() + " " + appliance.getHomeowner().getLastName());


            // Verify that the appliance belongs to the homeowner using email
            if (!appliance.getHomeowner().getEmail().equals(homeowner.getEmail())) {
                throw new AccessDeniedException("You can only create service requests for your own appliances");
            }

            // Validate preferred time slot (should be in the future)
            if (dto.getPreferredSlot().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Preferred time slot must be in the future");
            }

            ServiceRequest serviceRequest = new ServiceRequest();
            serviceRequest.setIssueDescription(dto.getIssueDescription());
            serviceRequest.setPreferredSlot(dto.getPreferredSlot());
            serviceRequest.setStatus(ServiceStatus.REQUESTED);
            serviceRequest.setHomeowner(homeowner);
            serviceRequest.setAppliance(appliance);
            serviceRequest.setCreatedAt(LocalDateTime.now());

            ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);
            System.out.println("Service request created successfully with ID: " + savedRequest.getId());
            return convertToDto(savedRequest);

        } catch (Exception e) {
            System.err.println("Error creating service request: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<ServiceRequestResponseDto> getHomeownerRequests(String email) {
        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));

        return serviceRequestRepository.findByHomeowner_Email(homeowner.getEmail())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ServiceRequestResponseDto getRequestById(Long requestId, String email) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found with id: " + requestId));

        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));

        // Verify that the service request belongs to the homeowner using email
        if (!serviceRequest.getHomeowner().getEmail().equals(homeowner.getEmail())) {
            throw new AccessDeniedException("You can only view your own service requests");
        }

        return convertToDto(serviceRequest);
    }

    public ServiceRequestResponseDto updateRequest(Long requestId, ServiceRequestDto dto, String email) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found with id: " + requestId));

        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));

        // Verify that the service request belongs to the homeowner using email
        if (!serviceRequest.getHomeowner().getEmail().equals(homeowner.getEmail())) {
            throw new AccessDeniedException("You can only update your own service requests");
        }

        // Only allow updates if the request is still in REQUESTED status
        if (serviceRequest.getStatus() != ServiceStatus.REQUESTED) {
            throw new IllegalStateException("Cannot update service request that is not in REQUESTED status");
        }

        // Validate preferred time slot (should be in the future)
        if (dto.getPreferredSlot().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Preferred time slot must be in the future");
        }

        serviceRequest.setIssueDescription(dto.getIssueDescription());
        serviceRequest.setPreferredSlot(dto.getPreferredSlot());

        ServiceRequest updatedRequest = serviceRequestRepository.save(serviceRequest);
        return convertToDto(updatedRequest);
    }

    public void cancelRequest(Long requestId, String email) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found with id: " + requestId));

        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));

        // Verify that the service request belongs to the homeowner using email
        if (!serviceRequest.getHomeowner().getEmail().equals(homeowner.getEmail())) {
            throw new AccessDeniedException("You can only cancel your own service requests");
        }

        // Only allow cancellation if the request is still in REQUESTED status
        if (serviceRequest.getStatus() != ServiceStatus.REQUESTED) {
            throw new IllegalStateException("Cannot cancel service request that is not in REQUESTED status");
        }

        serviceRequest.setStatus(ServiceStatus.CANCELLED);
        serviceRequestRepository.save(serviceRequest);
    }

    public List<ServiceRequestResponseDto> getAllRequests() {
        return serviceRequestRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ServiceRequestResponseDto assignTechnician(Long requestId, Long technicianId) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found with id: " + requestId));

        if (serviceRequest.getStatus() != ServiceStatus.REQUESTED) {
            throw new IllegalStateException("Can only assign technician to requests in REQUESTED status");
        }

        // Note: You would need to inject TechnicianRepository to get the technician
        // For now, we'll just update the status to ASSIGNED
        serviceRequest.setStatus(ServiceStatus.ASSIGNED);
        ServiceRequest updatedRequest = serviceRequestRepository.save(serviceRequest);
        return convertToDto(updatedRequest);
    }

    private ServiceRequestResponseDto convertToDto(ServiceRequest serviceRequest) {
        ServiceRequestResponseDto dto = new ServiceRequestResponseDto();
        dto.setId(serviceRequest.getId());
        dto.setIssueDescription(serviceRequest.getIssueDescription());
        dto.setPreferredSlot(serviceRequest.getPreferredSlot());
        dto.setStatus(serviceRequest.getStatus());
        dto.setCreatedAt(serviceRequest.getCreatedAt());

        // Set homeowner name
        if (serviceRequest.getHomeowner() != null) {
            dto.setHomeownerName(serviceRequest.getHomeowner().getFirstName() + " " + serviceRequest.getHomeowner().getLastName());
        }

        // Set appliance info
        if (serviceRequest.getAppliance() != null) {
            Appliance appliance = serviceRequest.getAppliance();
            dto.setApplianceInfo(appliance.getBrand() + " " + appliance.getModelNumber() + " (SN: " + appliance.getSerialNumber() + ")");
        }

        return dto;
    }
}
