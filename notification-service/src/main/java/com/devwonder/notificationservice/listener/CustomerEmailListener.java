package com.devwonder.notificationservice.listener;

import com.devwonder.common.event.CustomerCreatedEvent;
import com.devwonder.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerEmailListener {

    private final EmailService emailService;

    @KafkaListener(
        topics = "customer-created-notifications",
        groupId = "notification-service-group-customer",
        containerFactory = "customerCreatedKafkaListenerContainerFactory"
    )
    public void consumeCustomerCreatedNotification(CustomerCreatedEvent event) {
        try {
            log.info("Received customer created notification event for accountId: {} and customer: {}",
                event.getAccountId(), event.getCustomerName());

            log.info("Processing customer welcome email for customer: {}", event.getCustomerName());

            // Send welcome email with credentials
            emailService.sendCustomerWelcomeEmail(event);

            log.info("Successfully processed customer welcome email for accountId: {}",
                event.getAccountId());
        } catch (Exception e) {
            log.error("Error processing customer welcome email for accountId: {}",
                event.getAccountId(), e);
        }
    }
}