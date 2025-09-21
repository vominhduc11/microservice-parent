package com.devwonder.warrantyservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerWrapper {

    @NotNull(message = "Customer exists flag is required")
    private Boolean customerExists;

    @Valid
    private CustomerInfo customerInfo;

    private String customerIdentifier;
}