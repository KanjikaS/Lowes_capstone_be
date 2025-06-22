package com.capstone.warranty_tracker.service;

import com.capstone.warranty_tracker.dto.ApplianceRequestDto;
import com.capstone.warranty_tracker.model.Appliance;
import com.capstone.warranty_tracker.model.Homeowner;
import com.capstone.warranty_tracker.repository.ApplianceRepository;
import com.capstone.warranty_tracker.repository.HomeownerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ApplianceService {

    @Autowired
    private HomeownerRepository homeownerRepository;

    @Autowired
    private ApplianceRepository applianceRepository;

    public void addAppliance(ApplianceRequestDto dto, String email) {

    }
}
