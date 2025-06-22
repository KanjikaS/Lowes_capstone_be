package com.capstone.warranty_tracker.repository;

import com.capstone.warranty_tracker.model.Homeowner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HomeownerRepository extends JpaRepository<Homeowner, Long> {
    Optional<Homeowner> findByEmail(String email);
}
