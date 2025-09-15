package com.devwonder.mediaservice.listener;

import com.devwonder.mediaservice.dto.MediaUploadRequest;
import com.devwonder.mediaservice.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaMediaService {

    private final MediaService mediaService;

    /**
     * Listen for media upload requests from Product Service
     */
    @KafkaListener(
            topics = "media-upload-request",
            containerFactory = "mediaUploadRequestKafkaListenerContainerFactory"
    )
    public void handleMediaUploadRequest(Map<String, Object> requestData) {
        log.info("Received media upload request: {}", requestData);

        try {
            // Convert Map to MediaUploadRequest
            MediaUploadRequest request = mapToMediaUploadRequest(requestData);

            // Process the upload
            processMediaUpload(request);

            log.info("Successfully processed media upload for ID: {} (Product: {})",
                    request.getId(), request.getProductSku());

        } catch (Exception e) {
            log.error("Error processing media upload request: {}", e.getMessage(), e);
        }
    }

    /**
     * Process media upload request
     */
    private void processMediaUpload(MediaUploadRequest request) throws Exception {
        Map<String, Object> uploadResult;

        if (request.getMediaType() == MediaUploadRequest.MediaType.IMAGE) {
            // Upload image to Cloudinary using base64 method
            uploadResult = mediaService.uploadBase64Image(
                    request.getBase64Data(),
                    request.getFileName(),
                    request.getFolder()
            );
        } else {
            // For video, we might need to enhance MediaService to handle base64 video
            // For now, treat as image and let Cloudinary handle the format
            uploadResult = mediaService.uploadBase64Image(
                    request.getBase64Data(),
                    request.getFileName(),
                    request.getFolder()
            );
        }

        // Extract Cloudinary URL for logging
        String cloudinaryUrl = (String) uploadResult.get("secure_url");
        log.info("Successfully uploaded media to Cloudinary: {} for request ID: {} (Field: {})",
                cloudinaryUrl, request.getId(), request.getFieldPath());

        // TODO: In a complete implementation, you might want to:
        // 1. Send a callback event to Product Service with the Cloudinary URL
        // 2. Update the product record with the new URL
        // 3. Store upload metadata for tracking
    }

    /**
     * Convert Map to MediaUploadRequest object
     */
    private MediaUploadRequest mapToMediaUploadRequest(Map<String, Object> data) {
        return MediaUploadRequest.builder()
                .id((String) data.get("id"))
                .base64Data((String) data.get("base64Data"))
                .fileName((String) data.get("fileName"))
                .folder((String) data.get("folder"))
                .mediaType(MediaUploadRequest.MediaType.valueOf((String) data.get("mediaType")))
                .productSku((String) data.get("productSku"))
                .fieldPath((String) data.get("fieldPath"))
                .build();
    }
}