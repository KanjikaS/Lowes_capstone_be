package com.capstone.warranty_tracker.wrapper;
import com.capstone.warranty_tracker.dto.TechnicianAssignmentResponseDto;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianAssignmentWrapper {
    private String message;
    private List<TechnicianAssignmentResponseDto> assignments;
}
