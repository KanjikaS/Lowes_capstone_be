package com.capstone.warranty_tracker.repository;

import com.capstone.warranty_tracker.model.Technician;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnicianRepository extends JpaRepository<Technician, Long> {
}
