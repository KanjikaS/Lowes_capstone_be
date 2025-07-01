package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.*;
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

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ApplianceRepository applianceRepository;


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

    public List<ServiceRequestAdminDto> getAllServiceRequests(){
        return  serviceRequestRepository.findAll() .stream()
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
    @Transactional
    public boolean assignTechnicianToRequest(Long technicianId, Long requestId) {
        Optional<Technician> techOpt = technicianRepository.findById(technicianId);
        Optional<ServiceRequest> reqOpt = serviceRequestRepository.findById(requestId);




        if (techOpt.isEmpty() || reqOpt.isEmpty()) return false;

        Technician technician = techOpt.get();
        ServiceRequest request = reqOpt.get();
        System.out.println("hahahah");
        System.out.println(technician);
        System.out.println(request);



        // Check if already assigned
        if (request.getTechnician() != null) return false;

        // Assign technician
        request.setTechnician(technician);
        technician.getAssignedRequests().add(request);

        //  Change status to IN_PROGRESS
        request.setStatus(ServiceStatus.IN_PROGRESS);

        //  Save both entities
        serviceRequestRepository.save(request);
        technicianRepository.save(technician);

        return true;
    }


}
