package com.capstone.warranty_tracker.dto;

import java.time.LocalDateTime;

public record RescheduleDTO(
        LocalDateTime scheduledSlot
) {}

