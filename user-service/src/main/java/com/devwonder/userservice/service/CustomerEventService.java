package com.devwonder.userservice.service;

import com.devwonder.common.event.CustomerCreatedEvent;
import com.devwonder.userservice.constant.KafkaTopics;
import com.devwonder.userservice.entity.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerEventService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCustomerCreatedEvent(Customer customer, String username, String tempPassword) {
        try {
            CustomerCreatedEvent customerEvent = CustomerCreatedEvent.builder()
                    .accountId(customer.getAccountId())
                    .username(username)
                    .tempPassword(tempPassword)
                    .customerName(customer.getName())
                    .email(customer.getEmail())
                    .phone(customer.getPhone())
                    .address(customer.getAddress())
                    .creationTime(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(KafkaTopics.CUSTOMER_CREATED, customer.getAccountId().toString(), customerEvent);
            log.info("Published customer created event for accountId: {} with username: {}",
                     customer.getAccountId(), username);
        } catch (Exception e) {
            log.error("Failed to publish customer created event for accountId: {}, error: {}",
                      customer.getAccountId(), e.getMessage(), e);
            // Don't throw exception to prevent customer creation from failing due to notification issues
        }
    }
}