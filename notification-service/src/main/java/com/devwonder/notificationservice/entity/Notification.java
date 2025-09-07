package com.devwonder.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "id_user", nullable = false)
    private Long idUser;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    private NotificationType type = NotificationType.GENERAL;
    
    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.UNREAD;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    public enum NotificationType {
        GENERAL, ORDER, PRODUCT, PROMOTION, WARNING
    }
    
    public enum NotificationStatus {
        READ, UNREAD, ARCHIVED
    }
}