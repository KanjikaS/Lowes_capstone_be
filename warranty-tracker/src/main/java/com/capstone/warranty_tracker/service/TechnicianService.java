package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.model.Technician;
import com.capstone.warranty_tracker.repository.TechnicianRepository;
import com.capstone.warranty_tracker.dto.TechnicianResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class TechnicianService {

    @Autowired
    private TechnicianRepository technicianRepository;

    public List<TechnicianResponseDto> getAllTechnicians() {
        return technicianRepository.findAll().stream()
                .map(tech -> TechnicianResponseDto.builder()
                        .id(tech.getId())
                        .firstName(tech.getFirstName())
                        .lastName(tech.getLastName())
                        .email(tech.getEmail())
                        .phoneNumber(tech.getPhoneNumber())
                        .specialization(tech.getSpecialization())
                        .experience(tech.getExperience())
                        .build())
                .collect(Collectors.toList());
    }
    public List<TechnicianResponseDto> getAvailableTechnicians() {
        return technicianRepository.findTechniciansWithNoServiceRequests().stream()
                .map(tech -> TechnicianResponseDto.builder()
                        .id(tech.getId())
                        .firstName(tech.getFirstName())
                        .lastName(tech.getLastName())
                        .email(tech.getEmail())
                        .phoneNumber(tech.getPhoneNumber())
                        .specialization(tech.getSpecialization())
                        .experience(tech.getExperience())
                        .build())
                .collect(Collectors.toList());
    }

}
