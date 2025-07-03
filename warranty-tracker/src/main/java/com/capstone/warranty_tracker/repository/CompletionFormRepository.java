package com.capstone.warranty_tracker.repository;

import com.capstone.warranty_tracker.model.CompletionForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletionFormRepository extends JpaRepository<CompletionForm, Long> {
    CompletionForm findByServiceRequest_Id(Long serviceRequestId);
}
