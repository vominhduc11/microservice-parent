package com.devwonder.mediaservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic mediaUploadRequestTopic() {
        return TopicBuilder.name("media-upload-request")
                .partitions(3)
                .replicas(3)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .config(TopicConfig.RETENTION_MS_CONFIG, "3600000") // 1 hour retention
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2") // Minimum 2 replicas in sync
                .build();
    }
}