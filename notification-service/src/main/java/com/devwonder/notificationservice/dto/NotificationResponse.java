package com.devwonder.notificationservice.dto;

import com.devwonder.notificationservice.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private LocalDateTime time;
    private Boolean read;
    private String type;
    private LocalDateTime createdAt;

    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .time(notification.getTime())
                .read(notification.getRead())
                .type(notification.getType())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}