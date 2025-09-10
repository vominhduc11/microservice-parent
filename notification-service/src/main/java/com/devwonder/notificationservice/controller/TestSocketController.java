package com.devwonder.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @MessageMapping("/test-dealer")
    public void testSocket() {
        log.info("🧪 Testing WebSocket message handler");

        // Create notification payload for WebSocket broadcast
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "DEALER_REGISTRATION");
        notification.put("title", "New Dealer Registration");
        notification.put("message", "New dealer 'Test Company Ltd.' has been registered successfully");
        notification.put("timestamp", LocalDateTime.now());
        notification.put("data", Map.of(
            "accountId", 999L,
            "companyName", "Test Company Ltd.",
            "email", "test@company.com",
            "phone", "0123456789",
            "city", "Ho Chi Minh City",
            "district", "District 1",
            "registrationTime", LocalDateTime.now()
        ));
        
        // Use messagingTemplate for more control
        messagingTemplate.convertAndSend("/broadcast/dealer-registrations", notification);
        
        log.info("✅ WebSocket message handler executed and broadcasted successfully");
    }
    
    @PostMapping("/socket/direct")
    public ResponseEntity<Map<String, String>> testSocketDirect() {
        log.info("🧪 Testing direct WebSocket broadcast (traditional)");
        
        // Create test notification payload directly
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "TEST_DEALER_REGISTRATION");
        notification.put("title", "Test Dealer Registration");
        notification.put("message", "This is a direct WebSocket test message");
        notification.put("timestamp", LocalDateTime.now());
        notification.put("data", Map.of(
            "accountId", 888L,
            "companyName", "Direct Test Company",
            "email", "direct@test.com",
            "phone", "0987654321",
            "city", "Hanoi",
            "district", "Ba Dinh"
        ));
        
        // Send directly via SimpMessagingTemplate - traditional approach
        messagingTemplate.convertAndSend("/broadcast/dealer-registrations", notification);
        
        log.info("✅ Direct WebSocket broadcast sent successfully");
        
        return ResponseEntity.ok(Map.of(
            "message", "Direct WebSocket broadcast sent successfully",
            "type", "TRADITIONAL_BROADCAST",
            "topic", "/broadcast/dealer-registrations",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}