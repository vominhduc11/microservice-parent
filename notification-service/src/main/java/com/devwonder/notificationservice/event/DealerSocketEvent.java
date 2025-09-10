package com.devwonder.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealerSocketEvent {
    private Long accountId;
    private String companyName;
    private String email;
    private String phone;
    private String city;
    private String district;
    private LocalDateTime registrationTime;
}