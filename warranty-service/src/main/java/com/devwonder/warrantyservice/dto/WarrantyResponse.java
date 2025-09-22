package com.devwonder.warrantyservice.dto;

import com.devwonder.warrantyservice.enums.WarrantyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyResponse {

    private Long id;
    private Long idProductSerial;
    private Long idCustomer;
    private String warrantyCode;
    private WarrantyStatus status;
    private LocalDateTime purchaseDate;
    private LocalDateTime createAt;
}