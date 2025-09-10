package com.devwonder.notificationservice.service;

import com.devwonder.notificationservice.entity.Notification;
import com.devwonder.notificationservice.event.DealerSocketEvent;
import com.devwonder.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    public Notification createDealerRegistrationNotification(DealerSocketEvent event) {
        Notification notification = Notification.builder()
            .title("New Dealer Registration")
            .message(String.format("New dealer '%s' has been registered successfully", event.getCompanyName()))
            .type("DEALER_REGISTRATION")
            .read(false)
            .build();
            
        return notificationRepository.save(notification);
    }
    
    public List<Notification> getAllNotifications() {
        log.info("Fetching all notifications ordered by creation time");
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }
}