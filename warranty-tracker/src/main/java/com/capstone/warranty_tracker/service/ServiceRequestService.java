package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.AdminScheduleDto;
import com.capstone.warranty_tracker.dto.ServiceRequestDto;
import com.capstone.warranty_tracker.model.*;
import com.capstone.warranty_tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServiceRequestService {
    @Autowired private HomeownerRepository      homeownerRepository;
    @Autowired private ApplianceRepository      applianceRepository;
    @Autowired private TechnicianRepository     technicianRepository;
    @Autowired private ServiceRequestRepository serviceRequestRepository;

    public void createRequest(ServiceRequestDto dto, String email) {


        Homeowner homeowner = homeownerRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Homeowner not found"));


        Appliance appliance = applianceRepository
                .findById(dto.getApplianceId())
                .orElseThrow(() -> new RuntimeException("Appliance not found"));

        ServiceRequest sr = new ServiceRequest();
        sr.setIssueDescription(dto.getIssueDescription());
        sr.setPreferredSlot(dto.getPreferredSlot());
        sr.setStatus(ServiceStatus.REQUESTED);
        sr.setHomeowner(homeowner);
        sr.setAppliance(appliance);
        sr.setCreatedAt(LocalDateTime.now());

        serviceRequestRepository.save(sr);
    }


    public void reschedule(Long id, LocalDateTime newSlot, String email) {
        ServiceRequest sr = serviceRequestRepository.findById(id).orElseThrow();

        if (!sr.getHomeowner().getEmail().equals(email))
            throw new RuntimeException("Access denied");

        if (sr.getTechnician() != null && slotTaken(sr.getTechnician().getId(), newSlot))
            throw new RuntimeException("Technician already booked");

        sr.setPreferredSlot(newSlot);
        sr.setStatus(ServiceStatus.RESCHEDULED);
    }

    public void cancel(Long id, String email) {
        ServiceRequest sr = serviceRequestRepository.findById(id).orElseThrow();
        if (!sr.getHomeowner().getEmail().equals(email))
            throw new RuntimeException("Access denied");
        sr.setStatus(ServiceStatus.CANCELLED);
    }


    public void updateStatus(Long id, ServiceStatus status, String techEmail) {
        ServiceRequest sr = serviceRequestRepository.findById(id).orElseThrow();
        if (sr.getTechnician() == null ||
                !sr.getTechnician().getEmail().equals(techEmail))
            throw new RuntimeException("Only assigned technician can update");
        sr.setStatus(status);
    }


    public List<LocalDateTime> availableSlots(Long technicianId, LocalDate day) {
        LocalDateTime start = day.atTime(9, 0);
        List<LocalDateTime> all = new ArrayList<>();
        for (int h = 0; h < 9; h++)
            all.add(start.plusHours(h));

        return all.stream()
                .filter(s -> !slotTaken(technicianId, s))
                .collect(Collectors.toList());
    }

    private boolean slotTaken(Long techId, LocalDateTime slot) {
        return serviceRequestRepository.slotTaken(techId, slot, slot.plusHours(1));
    }

    public void technicianUpdateStatus(Long id,ServiceStatus s,String techEmail){
        ServiceRequest sr=serviceRequestRepository.findById(id).orElseThrow();
        if(sr.getTechnician()==null||!sr.getTechnician().getEmail().equals(techEmail)) throw new RuntimeException();
        sr.setStatus(s);
    }

    public void technicianReschedule(Long id,LocalDateTime newSlot,String techEmail){
        ServiceRequest sr=serviceRequestRepository.findById(id).orElseThrow();
        if(sr.getTechnician()==null||!sr.getTechnician().getEmail().equals(techEmail)) throw new RuntimeException();
        if(slotTaken(sr.getTechnician().getId(),newSlot)) throw new RuntimeException();
        sr.setPreferredSlot(newSlot);
        sr.setStatus(ServiceStatus.RESCHEDULED);
    }

    public void adminApprove(Long id){
        ServiceRequest sr=serviceRequestRepository.findById(id).orElseThrow();
        sr.setStatus(ServiceStatus.ASSIGNED);
    }

    public void adminCreate(AdminScheduleDto dto){
        Homeowner h=homeownerRepository.findById(dto.getHomeownerId()).orElseThrow();
        Appliance a=applianceRepository.findById(dto.getApplianceId()).orElseThrow();
        Technician t=technicianRepository.findById(dto.getTechnicianId()).orElseThrow();
        if(slotTaken(t.getId(),dto.getPreferredSlot())) throw new RuntimeException();
        ServiceRequest sr=new ServiceRequest();
        sr.setIssueDescription(dto.getIssueDescription());
        sr.setPreferredSlot(dto.getPreferredSlot());
        sr.setStatus(ServiceStatus.ASSIGNED);
        sr.setHomeowner(h);
        sr.setAppliance(a);
        sr.setTechnician(t);
        sr.setCreatedAt(LocalDateTime.now());
        serviceRequestRepository.save(sr);
    }

}

