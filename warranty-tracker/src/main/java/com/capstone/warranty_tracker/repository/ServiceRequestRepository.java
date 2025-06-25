package com.capstone.warranty_tracker.repository;

import com.capstone.warranty_tracker.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByHomeowner_Id(Long homeownerId);
    List<ServiceRequest> findByTechnician_Id(Long technicianId);

    @Query("""
           select count(sr) > 0 from ServiceRequest sr
           where sr.technician.id = :techId
           and sr.scheduledStart < :end
           and sr.scheduledStart + sr.durationMinutes * 1.0/1440 > :start
           """)
    boolean slotTaken(Long techId, LocalDateTime start, LocalDateTime end);
}

