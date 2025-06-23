//package com.capstone.warranty_tracker.dto;
//
//import lombok.Data;
//
//import java.time.LocalDate;
//
//@Data
//public class ApplianceRequestDto {
//    private String brand;
//    private String modelNumber;
//    private String serialNumber;
//    private LocalDate purchaseDate;
//    private String invoiceUrl;
//    private LocalDate warrantyExpiryDate;
//}
package com.capstone.warranty_tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ApplianceRequestDto {
    private String brand;
    private String modelNumber;
    private String serialNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    private String invoiceUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate warrantyExpiryDate;
}
