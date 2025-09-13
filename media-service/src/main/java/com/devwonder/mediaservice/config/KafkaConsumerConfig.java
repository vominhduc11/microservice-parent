package com.devwonder.mediaservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:kafka1:9092,kafka2:9093,kafka3:9094}")
    private String bootstrapServers;

    // Consumer Configuration - Only receiving messages
    private Map<String, Object> getBaseConsumerConfig(String groupId, String defaultType) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, defaultType);
        return configProps;
    }

    private ConcurrentKafkaListenerContainerFactory<String, Object> createListenerFactory(
            ConsumerFactory<String, Object> consumerFactory, int concurrency) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(concurrency);
        factory.setCommonErrorHandler(new DefaultErrorHandler());
        return factory;
    }

    // Media Upload Request Consumer
    @Bean
    public ConsumerFactory<String, Object> mediaUploadRequestConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(getBaseConsumerConfig("media-service-upload-group", "com.devwonder.mediaservice.dto.MediaUploadRequest"));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> mediaUploadRequestKafkaListenerContainerFactory() {
        return createListenerFactory(mediaUploadRequestConsumerFactory(), 2);
    }
}