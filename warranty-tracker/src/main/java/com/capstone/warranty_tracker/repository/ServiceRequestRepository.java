package com.capstone.warranty_tracker.repository;

import com.capstone.warranty_tracker.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByHomeowner_Id(Long homeownerId);
    List<ServiceRequest> findByTechnician_Id(Long technicianId);
}

