package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.*;
import com.capstone.warranty_tracker.dto.TechnicianAssignmentWrapper;
import com.capstone.warranty_tracker.model.ServiceRequest;
import com.capstone.warranty_tracker.model.ServiceStatus;
import com.capstone.warranty_tracker.model.Technician;
import com.capstone.warranty_tracker.repository.ApplianceRepository;
import com.capstone.warranty_tracker.repository.ServiceRequestRepository;
import com.capstone.warranty_tracker.repository.TechnicianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ApplianceRepository applianceRepository;

    @Transactional
    public TechnicianAssignmentWrapper assignTechniciansToUnassignedRequests() {
        List<Technician> availableTechs = technicianRepository.findTechniciansWithNoServiceRequests();
        List<ServiceRequest> unassignedRequests = serviceRequestRepository.findUnassignedRequests();

        List<TechnicianAssignmentResponseDto> assignmentLog = new ArrayList<>();

        if (unassignedRequests.isEmpty()) {
            return TechnicianAssignmentWrapper.builder()
                    .message("No unallocated service requests at the moment")
                    .assignments(List.of())
                    .build();
        }
        if (availableTechs.isEmpty()) {
            return TechnicianAssignmentWrapper.builder()
                    .message("No available technicians at the moment")
                    .assignments(List.of())
                    .build();
        }


        int techIndex = 0;
        for (ServiceRequest sr : unassignedRequests) {
            if (techIndex >= availableTechs.size()) break;

            Technician tech = availableTechs.get(techIndex);
            sr.setTechnician(tech);

            assignmentLog.add(new TechnicianAssignmentResponseDto(
                    sr.getId(),
                    tech.getId(),
                    tech.getFirstName() + " " + tech.getLastName(),
                    "Assigned successfully"
            ));

            techIndex++;
        }

        serviceRequestRepository.saveAll(unassignedRequests);

        String summaryMessage = (unassignedRequests.size() > availableTechs.size())
                ? "Some service requests remain unallocated. All technicians are currently busy."
                : "Technicians successfully assigned.";

        System.out.println(summaryMessage);


        return TechnicianAssignmentWrapper.builder()
                .message(summaryMessage)
                .assignments(assignmentLog)
                .build();
    }

    public List<ApplianceResponseDto> getAllAppliances() {
        return applianceRepository.findAll()
                .stream()
                .map(appliance -> new ApplianceResponseDto(
                        appliance.getId(),
                        appliance.getBrand(),
                        appliance.getCategory(),
                        appliance.getModelNumber(),
                        appliance.getSerialNumber(),
                        appliance.getPurchaseDate(),
                        appliance.getInvoiceUrl(),
                        appliance.getWarrantyExpiryDate(),
                        appliance.getHomeowner() != null
                                ? appliance.getHomeowner().getFirstName() + " " + appliance.getHomeowner().getLastName()
                                : "N/A"
                ))
                .collect(Collectors.toList());
    }
    public AdminStatsDto getStats(){
        //count no of appliance from appliance repo and return number
        //count no of service request where status is not equal to "COMPLETED"
        //count no of technicians from technician repo
        //count no of service reqyest where status is equal to "Completed"
        long applianceCount = applianceRepository.count();
        long technicianCount = technicianRepository.count();
        long pendingRequests = serviceRequestRepository.countByStatusNot(ServiceStatus.COMPLETED);
        long completedRequests = serviceRequestRepository.countByStatus(ServiceStatus.COMPLETED);
        System.out.println(pendingRequests + "mewo");
        return new AdminStatsDto(
                (int) technicianCount,
                (int) pendingRequests,
                (int) applianceCount,
                (int) completedRequests
        );
    }
    public List<ServiceRequestAdminDto> getRecentServiceRequest() {
        return serviceRequestRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(sr -> new ServiceRequestAdminDto(
                        sr.getId(),
                        sr.getAppliance().getBrand() + " " + sr.getAppliance().getModelNumber(),
                        sr.getAppliance().getSerialNumber(),
                        sr.getHomeowner().getFirstName() + " " + sr.getHomeowner().getLastName(),
                        sr.getTechnician() != null
                                ? sr.getTechnician().getFirstName() + " " + sr.getTechnician().getLastName()
                                : "-",
                        sr.getStatus().name(),
                        sr.getCreatedAt()
                ))
                .toList();
    }
}
