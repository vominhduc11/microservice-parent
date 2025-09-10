package com.devwonder.userservice.service;

import com.devwonder.userservice.entity.Dealer;
import com.devwonder.userservice.event.DealerEmailEvent;
import com.devwonder.userservice.event.DealerSocketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealerEventService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishDealerEmailEvent(Dealer dealer, String username, String password) throws Exception {
        DealerEmailEvent emailEvent = DealerEmailEvent.builder()
                .accountId(dealer.getAccountId())
                .username(username)
                .password(password)
                .companyName(dealer.getCompanyName())
                .email(dealer.getEmail())
                .phone(dealer.getPhone())
                .address(dealer.getAddress())
                .city(dealer.getCity())
                .district(dealer.getDistrict())
                .registrationTime(LocalDateTime.now())
                .build();
        
        kafkaTemplate.send("email-notifications", dealer.getAccountId().toString(), emailEvent);
        log.info("Published dealer email event for accountId: {}", dealer.getAccountId());
    }
    
    public void publishDealerSocketEvent(Dealer dealer) throws Exception {
        DealerSocketEvent socketEvent = DealerSocketEvent.builder()
                .accountId(dealer.getAccountId())
                .companyName(dealer.getCompanyName())
                .email(dealer.getEmail())
                .phone(dealer.getPhone())
                .city(dealer.getCity())
                .district(dealer.getDistrict())
                .registrationTime(LocalDateTime.now())
                .build();
        
        kafkaTemplate.send("websocket-notifications", dealer.getAccountId().toString(), socketEvent);
        log.info("Published dealer socket event for accountId: {}", dealer.getAccountId());
    }
}