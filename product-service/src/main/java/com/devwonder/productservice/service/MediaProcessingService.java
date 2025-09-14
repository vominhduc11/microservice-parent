package com.devwonder.productservice.service;

import com.devwonder.productservice.constant.KafkaTopics;
import com.devwonder.productservice.dto.MediaUploadRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaProcessingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Process product data and send base64 media to Kafka for async processing
     */
    public void processProductMediaAsync(String productSku, Object description, Object videos, String image) {
        try {
            // Process description array
            if (description != null) {
                processDescriptionAsync(productSku, description);
            }

            // Process videos array
            if (videos != null) {
                processVideosAsync(productSku, videos);
            }

            // Process main image (if it's base64)
            if (image != null && isBase64Data(image)) {
                sendMediaUploadRequest(productSku, image, "main_image", "products/main", MediaUploadRequest.MediaType.IMAGE, "images");
                log.info("Sent main image upload request to Kafka for product: {}", productSku);
            }

        } catch (Exception e) {
            log.error("Error processing product media: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process media data", e);
        }
    }

    /**
     * Process description array and send base64 images to Kafka
     */
    public Object processDescriptionAsync(String productSku, Object descriptionData) {
        if (descriptionData == null) {
            return null;
        }

        try {
            JsonNode descriptionArray = objectMapper.valueToTree(descriptionData);
            if (!descriptionArray.isArray()) {
                return descriptionData;
            }

            ArrayNode processedArray = objectMapper.createArrayNode();

            for (int i = 0; i < descriptionArray.size(); i++) {
                JsonNode item = descriptionArray.get(i);
                ObjectNode processedItem = item.deepCopy();

                // Check if type is "image" and link contains base64 data
                if (item.has("type") && "image".equals(item.get("type").asText()) &&
                    item.has("link") && isBase64Data(item.get("link").asText())) {

                    String base64Data = item.get("link").asText();
                    String fileName = generateFileName("desc_image");

                    // Send to Kafka for async processing
                    sendMediaUploadRequest(productSku, base64Data, fileName, "products/description",
                                         MediaUploadRequest.MediaType.IMAGE, "description[" + i + "].link");

                    // Keep original data for now (will be updated by callback)
                    log.info("Sent description image upload request to Kafka: {}", fileName);
                }

                processedArray.add(processedItem);
            }

            return objectMapper.treeToValue(processedArray, Object.class);

        } catch (Exception e) {
            log.error("Error processing description media: {}", e.getMessage(), e);
            return descriptionData;
        }
    }

    /**
     * Process videos array and send base64 videos to Kafka
     */
    public Object processVideosAsync(String productSku, Object videosData) {
        if (videosData == null) {
            return null;
        }

        try {
            JsonNode videosArray = objectMapper.valueToTree(videosData);
            if (!videosArray.isArray()) {
                return videosData;
            }

            ArrayNode processedArray = objectMapper.createArrayNode();

            for (int i = 0; i < videosArray.size(); i++) {
                JsonNode video = videosArray.get(i);
                ObjectNode processedVideo = video.deepCopy();

                // Check if videoUrl contains base64 data
                if (video.has("videoUrl") && isBase64Data(video.get("videoUrl").asText())) {
                    String base64Data = video.get("videoUrl").asText();
                    String fileName = generateFileName("video");

                    // Send to Kafka for async processing
                    sendMediaUploadRequest(productSku, base64Data, fileName, "products/videos",
                                         MediaUploadRequest.MediaType.VIDEO, "videos[" + i + "].videoUrl");

                    log.info("Sent video upload request to Kafka: {}", fileName);
                }

                processedArray.add(processedVideo);
            }

            return objectMapper.treeToValue(processedArray, Object.class);

        } catch (Exception e) {
            log.error("Error processing videos media: {}", e.getMessage(), e);
            return videosData;
        }
    }

    /**
     * Process main image - send to Kafka if base64, return as-is if URL
     */
    public String processMainImage(String image) {
        if (image == null || !isBase64Data(image)) {
            return image;
        }

        // For async processing, we return original and process in background
        return image;
    }

    /**
     * Process main image async - send to Kafka for processing
     */
    public String processMainImageAsync(String productSku, String image) {
        if (image == null || !isBase64Data(image)) {
            return image;
        }

        try {
            String fileName = generateFileName("main_image");
            sendMediaUploadRequest(productSku, image, fileName, "products/main",
                                 MediaUploadRequest.MediaType.IMAGE, "images");
            log.info("Sent main image upload request to Kafka: {}", fileName);

            return image; // Return original, will be updated by callback later
        } catch (Exception e) {
            log.error("Error processing main image: {}", e.getMessage(), e);
            return image;
        }
    }

    /**
     * Check if string is base64 data
     */
    private boolean isBase64Data(String data) {
        if (data == null || data.length() < 100) {
            return false;
        }

        // Check for data URL format
        if (data.startsWith("data:image/") || data.startsWith("data:video/")) {
            return data.contains("base64,");
        }

        // Check for raw base64 (basic validation)
        String cleanData = data.replaceAll("[^A-Za-z0-9+/=]", "");
        return cleanData.length() > 100 && cleanData.equals(data.replaceAll("[^A-Za-z0-9+/=\\s]", "").replaceAll("\\s", ""));
    }

    /**
     * Generate unique filename
     */
    private String generateFileName(String prefix) {
        return String.format("%s_%s", prefix, UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * Send media upload request to Kafka
     */
    private void sendMediaUploadRequest(String productSku, String base64Data, String fileName,
                                      String folder, MediaUploadRequest.MediaType mediaType, String fieldPath) {
        try {
            // Clean base64 data (remove data URL prefix if present)
            String cleanBase64 = base64Data;
            if (base64Data.contains(",")) {
                cleanBase64 = base64Data.substring(base64Data.indexOf(",") + 1);
            }

            MediaUploadRequest request = MediaUploadRequest.builder()
                    .id(UUID.randomUUID().toString())
                    .base64Data(cleanBase64)
                    .fileName(fileName)
                    .folder(folder)
                    .mediaType(mediaType)
                    .productSku(productSku)
                    .fieldPath(fieldPath)
                    .build();

            kafkaTemplate.send(KafkaTopics.MEDIA_UPLOAD_REQUEST, request.getId(), request);
            log.info("Sent media upload request to Kafka - ID: {}, SKU: {}, Field: {}",
                    request.getId(), productSku, fieldPath);

        } catch (Exception e) {
            log.error("Error sending media upload request to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send media upload request", e);
        }
    }

    // Sync versions for backward compatibility (can be removed if going fully async)
    public Object processDescription(Object descriptionData) {
        return processDescriptionAsync("temp", descriptionData);
    }

    public Object processVideos(Object videosData) {
        return processVideosAsync("temp", videosData);
    }
}