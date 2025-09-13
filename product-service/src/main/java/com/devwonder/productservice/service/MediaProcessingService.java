package com.devwonder.productservice.service;

import com.devwonder.productservice.constant.KafkaTopics;
import com.devwonder.productservice.dto.MediaUploadRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaProcessingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final Pattern BASE64_PATTERN = Pattern.compile("data:image/([a-zA-Z]+);base64,(.+)");

    /**
     * Process base64 images and send to Media Service for async upload
     * Returns original data since we don't wait for response
     */
    public void processProductMedia(Object productData) {
        if (productData == null) return;

        try {
            String jsonData = objectMapper.writeValueAsString(productData);
            processBase64ImagesAsync(jsonData);
        } catch (Exception e) {
            log.error("Error processing product media", e);
        }
    }

    /**
     * Detect base64 images and send them for async upload
     */
    private void processBase64ImagesAsync(String jsonData) {
        Matcher matcher = BASE64_PATTERN.matcher(jsonData);

        while (matcher.find()) {
            String imageFormat = matcher.group(1);
            String base64Data = matcher.group(2);

            try {
                // Send to Kafka for async upload
                String uploadId = UUID.randomUUID().toString();
                MediaUploadRequest uploadRequest = MediaUploadRequest.builder()
                        .id(uploadId)
                        .base64Data(base64Data)
                        .fileName("product_image_" + uploadId + "." + imageFormat)
                        .folder("products")
                        .mediaType(MediaUploadRequest.MediaType.IMAGE)
                        .build();

                // Fire-and-forget: send message without waiting for response
                kafkaTemplate.send(KafkaTopics.MEDIA_UPLOAD_REQUEST, uploadRequest);
                log.info("Sent async media upload request for ID: {}", uploadId);

            } catch (Exception e) {
                log.error("Error sending media upload request", e);
            }
        }
    }

    /**
     * Process a single base64 image (async, no response expected)
     */
    public void processBase64ImageAsync(String base64Data, String folder) {
        if (base64Data == null || !containsBase64Images(base64Data)) {
            return;
        }

        try {
            Matcher matcher = BASE64_PATTERN.matcher(base64Data);
            if (matcher.find()) {
                String imageFormat = matcher.group(1);
                String base64Content = matcher.group(2);

                String uploadId = UUID.randomUUID().toString();
                MediaUploadRequest uploadRequest = MediaUploadRequest.builder()
                        .id(uploadId)
                        .base64Data(base64Content)
                        .fileName("product_image_" + uploadId + "." + imageFormat)
                        .folder(folder)
                        .mediaType(MediaUploadRequest.MediaType.IMAGE)
                        .build();

                kafkaTemplate.send(KafkaTopics.MEDIA_UPLOAD_REQUEST, uploadRequest);
                log.info("Sent async single media upload request for ID: {}", uploadId);
            }
        } catch (Exception e) {
            log.error("Error processing single base64 image", e);
        }
    }

    /**
     * Process base64 images in JSON string (returns original - async upload)
     */
    public String processBase64Images(String jsonData) {
        // Process async but return original data immediately
        processBase64ImagesAsync(jsonData);
        return jsonData; // Return original since we don't wait for upload
    }

    /**
     * Check if a string contains base64 image data
     */
    public boolean containsBase64Images(String data) {
        if (data == null) return false;
        return BASE64_PATTERN.matcher(data).find();
    }
}