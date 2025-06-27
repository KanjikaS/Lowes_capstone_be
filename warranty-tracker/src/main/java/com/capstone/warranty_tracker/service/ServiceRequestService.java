package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.AdminScheduleDto;
import com.capstone.warranty_tracker.dto.ServiceRequestDto;
import com.capstone.warranty_tracker.dto.ServiceRequestResponseDto;
import com.capstone.warranty_tracker.model.*;
import org.springframework.security.access.AccessDeniedException;
import com.capstone.warranty_tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServiceRequestService {
    @Autowired private HomeownerRepository homeownerRepository;
    @Autowired private ApplianceRepository applianceRepository;
    @Autowired private TechnicianRepository technicianRepository;
    @Autowired private ServiceRequestRepository serviceRequestRepository;
    @Autowired private UserRepository userRepository;


    public ServiceRequestResponseDto createRequest(ServiceRequestDto dto, String email) throws AccessDeniedException {
        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));

        Appliance appliance;
        if (dto.getSerialNumber() != null && !dto.getSerialNumber().isEmpty()) {
            appliance = applianceRepository.findBySerialNumber(dto.getSerialNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Appliance not found with serial number: " + dto.getSerialNumber()));
        } else {
            appliance = applianceRepository.findById(dto.getApplianceId())
                    .orElseThrow(() -> new IllegalArgumentException("Appliance not found with id: " + dto.getApplianceId()));
        }

        if (!appliance.getHomeowner().getEmail().equals(homeowner.getEmail())) {
            throw new AccessDeniedException("You can only create service requests for your own appliances");
        }

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

        return convertToDto(serviceRequestRepository.save(serviceRequest));
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

        if (!serviceRequest.getHomeowner().getEmail().equals(homeowner.getEmail())) {
            throw new AccessDeniedException("You can only update your own service requests");
        }

        if (serviceRequest.getStatus() != ServiceStatus.REQUESTED) {
            throw new IllegalStateException("Cannot update service request that is not in REQUESTED status");
        }

        if (dto.getPreferredSlot().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Preferred time slot must be in the future");
        }

        serviceRequest.setIssueDescription(dto.getIssueDescription());
        serviceRequest.setPreferredSlot(dto.getPreferredSlot());

        return convertToDto(serviceRequestRepository.save(serviceRequest));
    }

    public void cancelRequest(Long requestId, String email) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found with id: " + requestId));

        Homeowner homeowner = (Homeowner) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Homeowner not found with email: " + email));

        if (!serviceRequest.getHomeowner().getEmail().equals(homeowner.getEmail())) {
            throw new AccessDeniedException("You can only cancel your own service requests");
        }

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

        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new IllegalArgumentException("Technician not found"));

        serviceRequest.setTechnician(technician);
        serviceRequest.setStatus(ServiceStatus.ASSIGNED);

        return convertToDto(serviceRequestRepository.save(serviceRequest));
    }


    public void reschedule(Long id, LocalDateTime newSlot, String email) {
        ServiceRequest sr = serviceRequestRepository.findById(id).orElseThrow();
        if (!sr.getHomeowner().getEmail().equals(email)) throw new RuntimeException("Access denied");
        if (sr.getTechnician() != null && slotTaken(sr.getTechnician().getId(), newSlot)) throw new RuntimeException("Technician already booked");
        sr.setPreferredSlot(newSlot);
        sr.setStatus(ServiceStatus.RESCHEDULED);
    }

    public void cancel(Long id, String email) {
        ServiceRequest sr = serviceRequestRepository.findById(id).orElseThrow();
        if (!sr.getHomeowner().getEmail().equals(email)) throw new RuntimeException("Access denied");
        sr.setStatus(ServiceStatus.CANCELLED);
    }

    public void updateStatus(Long id, ServiceStatus status, String techEmail) {
        ServiceRequest sr = serviceRequestRepository.findById(id).orElseThrow();
        if (sr.getTechnician() == null || !sr.getTechnician().getEmail().equals(techEmail)) throw new RuntimeException("Only assigned technician can update");
        sr.setStatus(status);
    }

    public List<LocalDateTime> availableSlots(Long technicianId, LocalDate day) {
        LocalDateTime start = day.atTime(9, 0);
        List<LocalDateTime> all = new ArrayList<>();
        for (int h = 0; h < 9; h++) all.add(start.plusHours(h));
        return all.stream().filter(s -> !slotTaken(technicianId, s)).collect(Collectors.toList());
    }

    private boolean slotTaken(Long techId, LocalDateTime slot) {
        return serviceRequestRepository.slotTaken(techId, slot, slot.plusHours(1));
    }


    public void technicianUpdateStatus(Long id, ServiceStatus status, String techEmail) {
        ServiceRequest sr = serviceRequestRepository.findById(id).orElseThrow();
        if (sr.getTechnician() == null || !sr.getTechnician().getEmail().equals(techEmail)) throw new RuntimeException();
        sr.setStatus(status);
    }

    public void technicianReschedule(Long id, LocalDateTime newSlot, String techEmail) {
        ServiceRequest sr = serviceRequestRepository.findById(id).orElseThrow();
        if (sr.getTechnician() == null || !sr.getTechnician().getEmail().equals(techEmail)) throw new RuntimeException();
        if (slotTaken(sr.getTechnician().getId(), newSlot)) throw new RuntimeException("Slot already booked");
        sr.setPreferredSlot(newSlot);
        sr.setStatus(ServiceStatus.RESCHEDULED);
    }


    private ServiceRequestResponseDto convertToDto(ServiceRequest serviceRequest) {
        ServiceRequestResponseDto dto = new ServiceRequestResponseDto();
        dto.setId(serviceRequest.getId());
        dto.setIssueDescription(serviceRequest.getIssueDescription());
        dto.setPreferredSlot(serviceRequest.getPreferredSlot());
        dto.setStatus(serviceRequest.getStatus());
        dto.setCreatedAt(serviceRequest.getCreatedAt());

        if (serviceRequest.getHomeowner() != null) {
            dto.setHomeownerName(serviceRequest.getHomeowner().getFirstName() + " " + serviceRequest.getHomeowner().getLastName());
        }

        if (serviceRequest.getAppliance() != null) {
            Appliance appliance = serviceRequest.getAppliance();
            dto.setApplianceInfo(appliance.getBrand() + " " + appliance.getModelNumber() + " (SN: " + appliance.getSerialNumber() + ")");
        }

        return dto;
    }
}


