package com.devwonder.warrantyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyBulkCreateResponse {

    private Long customerId;
    private String customerName;
    private List<WarrantyResponse> warranties;
    private Integer totalWarranties;
    private List<String> failedSerials;
}