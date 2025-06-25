package com.capstone.warranty_tracker.repository;

import com.capstone.warranty_tracker.model.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    @Query("SELECT t FROM Technician t WHERE t.assignedRequests IS EMPTY")
    List<Technician> findTechniciansWithNoServiceRequests();
}
