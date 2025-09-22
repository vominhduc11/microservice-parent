package com.devwonder.warrantyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckCustomerExistsResponse {

    private boolean exists;
    private String matchedBy;
    private String message;
    private CustomerInfo customerInfo;
}