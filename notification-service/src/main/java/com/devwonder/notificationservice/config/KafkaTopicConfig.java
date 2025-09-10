package com.devwonder.notificationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    
    @Bean
    public NewTopic emailNotificationTopic() {
        return TopicBuilder.name("email-notifications")
                .partitions(3)
                .replicas(3)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 days retention
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2") // Minimum 2 replicas in sync
                .build();
    }
    
    @Bean
    public NewTopic websocketNotificationTopic() {
        return TopicBuilder.name("websocket-notifications")
                .partitions(3)
                .replicas(3)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .config(TopicConfig.RETENTION_MS_CONFIG, "86400000") // 1 day retention (shorter for real-time notifications)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2") // Minimum 2 replicas in sync
                .build();
    }
}