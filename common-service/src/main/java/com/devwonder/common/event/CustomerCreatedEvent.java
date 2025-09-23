package com.devwonder.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreatedEvent {
    private Long accountId;
    private String username;
    private String tempPassword;
    private String customerName;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime creationTime;
}