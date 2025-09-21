package com.devwonder.userservice.dto;

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
    private String matchedBy; // "email", "phone", or null
    private String message;
    private CustomerInfo customerInfo; // full customer information if exists
}