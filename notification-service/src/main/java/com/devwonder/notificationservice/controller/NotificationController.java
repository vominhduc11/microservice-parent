package com.devwonder.notificationservice.controller;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.notificationservice.entity.Notification;
import com.devwonder.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping("/notifies")
    public ResponseEntity<BaseResponse<List<Notification>>> getAllNotifications() {
        
        log.info("Requesting all notifications");
        
        List<Notification> notifications = notificationService.getAllNotifications();
        
        log.info("Retrieved {} notifications", notifications.size());
        
        return ResponseEntity.ok(BaseResponse.success("Notifications retrieved successfully", notifications));
    }
}