package com.capstone.warranty_tracker.repository;

import com.capstone.warranty_tracker.model.Appliance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplianceRepository extends JpaRepository<Appliance, Long> {
    List<Appliance> findByHomeowner_Id(Long homeownerId);
}
