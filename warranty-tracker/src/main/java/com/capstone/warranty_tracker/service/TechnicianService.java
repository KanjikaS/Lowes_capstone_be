package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.*;
import com.capstone.warranty_tracker.model.ServiceRequest;
import com.capstone.warranty_tracker.model.ServiceStatus;
import com.capstone.warranty_tracker.model.Technician;
import com.capstone.warranty_tracker.repository.ServiceRequestRepository;
import com.capstone.warranty_tracker.repository.TechnicianRepository;
import com.capstone.warranty_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.util.List;

import com.capstone.warranty_tracker.model.CompletionForm;
import com.capstone.warranty_tracker.repository.CompletionFormRepository;


@Service
public class TechnicianService {

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ServiceRequestService serviceRequestService;

    public List<TechnicianResponseDto> getAllTechnicians() {
        return technicianRepository.findAll().stream()
                .map(tech -> TechnicianResponseDto.builder()
                        .id(tech.getId())
                        .firstName(tech.getFirstName())
                        .lastName(tech.getLastName())
                        .email(tech.getEmail())
                        .phoneNumber(tech.getPhoneNumber())
                        .specialization(tech.getSpecialization())
                        .experience(tech.getExperience())
                        .build())
                .collect(Collectors.toList());
    }
    public List<TechnicianResponseDto> getAvailableTechnicians() {
        return technicianRepository.findTechniciansWithNoServiceRequests().stream()
                .map(tech -> TechnicianResponseDto.builder()
                        .id(tech.getId())
                        .firstName(tech.getFirstName())
                        .lastName(tech.getLastName())
                        .email(tech.getEmail())
                        .phoneNumber(tech.getPhoneNumber())
                        .specialization(tech.getSpecialization())
                        .experience(tech.getExperience())
                        .build())
                .collect(Collectors.toList());
    }

    public List<ServiceRequestResponseDto> getAssignedRequestsForTechnician(String email) {
        List<ServiceRequest> requests = serviceRequestRepository.findAssignedRequestsByTechnicianEmail(email);

        return requests.stream().map(sr -> {
            String homeownerName = sr.getHomeowner().getFirstName() + " " + sr.getHomeowner().getLastName();
            String applianceInfo = sr.getAppliance().getBrand() + " " +
                    sr.getAppliance().getModelNumber() + " (" +
                    sr.getAppliance().getSerialNumber() + ")";
            return new ServiceRequestResponseDto(
                    sr.getId(),
                    sr.getIssueDescription(),
                    sr.getPreferredSlot(),
                    sr.getStatus(),
                    homeownerName,
                    applianceInfo,
                    sr.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }

    public void updateRequestStatus(String technicianEmail, UpdateRequestStatusDto dto) {
        ServiceRequest request = serviceRequestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Service Request not found"));

        // Ensure the logged-in technician is assigned to this request
        if (request.getTechnician() == null || !request.getTechnician().getEmail().equals(technicianEmail)) {
            throw new RuntimeException("Unauthorized: This request is not assigned to you.");
        }

        // Restrict to valid technician statuses
        ServiceStatus newStatus = dto.getStatus();
        if (newStatus != ServiceStatus.IN_PROGRESS &&
                newStatus != ServiceStatus.COMPLETED) {
            throw new RuntimeException("Technicians can only set status to IN_PROGRESS or COMPLETED");
        }

        request.setStatus(newStatus);
        serviceRequestRepository.save(request);
    }

    public List<ServiceRequestResponseDto> getInProgressRequestsForTechnician(String email) {
        List<ServiceRequest> requests = serviceRequestRepository.findAssignedRequestsByTechnicianEmail(email);

        return requests.stream()
                .filter(sr -> sr.getStatus() == ServiceStatus.IN_PROGRESS)
                .map(sr -> {
                    String homeownerName = sr.getHomeowner().getFirstName() + " " + sr.getHomeowner().getLastName();
                    String applianceInfo = sr.getAppliance().getBrand() + " " +
                            sr.getAppliance().getModelNumber() + " (" +
                            sr.getAppliance().getSerialNumber() + ")";
                    return new ServiceRequestResponseDto(
                            sr.getId(),
                            sr.getIssueDescription(),
                            sr.getPreferredSlot(),
                            sr.getStatus(),
                            homeownerName,
                            applianceInfo,
                            sr.getCreatedAt()
                    );
                }).collect(Collectors.toList());
    }

    public List<ServiceRequestResponseDto> getCompletedRequestsForTechnician(String email) {
        List<ServiceRequest> requests = serviceRequestRepository.findAssignedRequestsByTechnicianEmail(email)
                .stream()
                .filter(req -> req.getStatus() == ServiceStatus.COMPLETED)
                .collect(Collectors.toList());

        return requests.stream().map(sr -> {
            String homeownerName = sr.getHomeowner().getFirstName() + " " + sr.getHomeowner().getLastName();
            String applianceInfo = sr.getAppliance().getBrand() + " " +
                    sr.getAppliance().getModelNumber() + " (" +
                    sr.getAppliance().getSerialNumber() + ")";
            return new ServiceRequestResponseDto(
                    sr.getId(),
                    sr.getIssueDescription(),
                    sr.getPreferredSlot(),
                    sr.getStatus(),
                    homeownerName,
                    applianceInfo,
                    sr.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }

    public TechnicianResponseDto getTechnicianProfile(String email) {
        Technician technician = technicianRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Technician not found"));

        return TechnicianResponseDto.builder()
                .id(technician.getId())
                .firstName(technician.getFirstName())
                .lastName(technician.getLastName())
                .email(technician.getEmail())
                .phoneNumber(technician.getPhoneNumber())
                .specialization(technician.getSpecialization())
                .experience(technician.getExperience())
                .build();
    }

    public List<ServiceHistoryDto> getServiceHistoryByTechnician_Id(Long technicianId) {
        return serviceRequestService.getServiceHistoryByTechnician_Id(technicianId);
    }

    public TechnicianStatsDto getTechnicianStats(String email) {
        List<ServiceRequest> requests = serviceRequestRepository.findAssignedRequestsByTechnicianEmail(email);

        long assignedCount = requests.stream().filter(r -> r.getStatus() == ServiceStatus.ASSIGNED).count();
        long inProgressCount = requests.stream().filter(r -> r.getStatus() == ServiceStatus.IN_PROGRESS).count();
        long completedCount = requests.stream().filter(r -> r.getStatus() == ServiceStatus.COMPLETED).count();

        return TechnicianStatsDto.builder()
                .assignedCount(assignedCount)
                .inProgressCount(inProgressCount)
                .completedCount(completedCount)
                .build();
    }




    public  List<ServiceRequestResponseDto> getAssignedRequestsForTechnicianByID(Long technicianId){
        List<ServiceRequest> requests = serviceRequestRepository.findByTechnician_Id(technicianId);

        return requests.stream()
                .filter(sr -> sr.getStatus() == ServiceStatus.ASSIGNED)
                .map(sr -> {
            String homeownerName = sr.getHomeowner().getFirstName() + " " + sr.getHomeowner().getLastName();
            String applianceInfo = sr.getAppliance().getBrand() + " " +
                    sr.getAppliance().getModelNumber() + " (" +
                    sr.getAppliance().getSerialNumber() + ")";
            return new ServiceRequestResponseDto(
                    sr.getId(),
                    sr.getIssueDescription(),
                    sr.getPreferredSlot(),
                    sr.getStatus(),
                    homeownerName,
                    applianceInfo,
                    sr.getCreatedAt()
            );
        }).collect(Collectors.toList());

    }

    @Autowired
    private CompletionFormRepository completionFormRepository;

    @Autowired
    private UserRepository userRepository;


    public void saveCompletionForm(Long requestId, CompletionFormDto dto, String technicianEmail) {
        Technician technician = (Technician) userRepository.findByEmail(technicianEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Technician not found"));

        ServiceRequest sr = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (sr.getTechnician() == null) {
            throw new IllegalStateException("This request is not assigned to any technician.");
        }

        if (!sr.getTechnician().getId().equals(technician.getId())) {
            throw new SecurityException("You are not assigned to this request.");
        }

        CompletionForm form = new CompletionForm();
        form.setCompletionDate(dto.getCompletionDate());
        form.setCompletionTime(dto.getCompletionTime());
        form.setTechnicianNotes(dto.getTechnicianNotes());
        form.setConfirmed(dto.isConfirmed());
        form.setServiceRequest(sr);
        form.setTechnician(technician);

        completionFormRepository.save(form);
    }

    public CompletionFormResponseDto getCompletionForm(Long requestId, String technicianEmail) {
        Technician technician = (Technician) userRepository.findByEmail(technicianEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Technician not found"));

        CompletionForm form = completionFormRepository.findByServiceRequest_Id(requestId);
        if (form == null) throw new IllegalArgumentException("Form not found.");

        if (!form.getTechnician().getId().equals(technician.getId())) {
            throw new SecurityException("Not authorized.");
        }

        CompletionFormResponseDto dto = new CompletionFormResponseDto();
        dto.setId(form.getId());
        dto.setCompletionDate(form.getCompletionDate());
        dto.setCompletionTime(form.getCompletionTime());
        dto.setTechnicianNotes(form.getTechnicianNotes());
        dto.setConfirmed(form.isConfirmed());
        dto.setServiceRequestId(form.getServiceRequest().getId());
        dto.setTechnicianId(form.getTechnician().getId());
        dto.setTechnicianEmail(form.getTechnician().getEmail());

        return dto;
    }










}
