package com.capstone.warranty_tracker.repository;

import com.capstone.warranty_tracker.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByHomeowner_Id(Long homeownerId);
    List<ServiceRequest> findByHomeowner_Email(String email);
    List<ServiceRequest> findByTechnician_Id(Long technicianId);

    List<ServiceRequest> findByApplianceId(Long id);

    List<ServiceRequest> findByHomeownerId(Long homeownerId);
}

