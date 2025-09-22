package com.devwonder.notificationservice.controller;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.notificationservice.dto.NotificationResponse;
import com.devwonder.notificationservice.entity.Notification;
import com.devwonder.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping("/notifies")
    public ResponseEntity<BaseResponse<List<NotificationResponse>>> getAllNotifications() {

        log.info("Requesting all notifications");

        List<Notification> notifications = notificationService.getAllNotifications();
        List<NotificationResponse> response = notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());

        log.info("Retrieved {} notifications", notifications.size());

        return ResponseEntity.ok(BaseResponse.success("Notifications retrieved successfully", response));
    }
    
    @PatchMapping("/{id}/read")
    public ResponseEntity<BaseResponse<NotificationResponse>> markNotificationAsRead(@PathVariable Long id) {

        log.info("Marking notification {} as read", id);

        Notification updatedNotification = notificationService.markAsRead(id);
        NotificationResponse response = NotificationResponse.fromEntity(updatedNotification);

        log.info("Successfully marked notification {} as read", id);

        return ResponseEntity.ok(BaseResponse.success("Notification marked as read", response));
    }
}