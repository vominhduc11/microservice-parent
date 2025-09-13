package com.devwonder.mediaservice.service;

import com.devwonder.mediaservice.constant.KafkaTopics;
import com.devwonder.mediaservice.dto.MediaUploadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaMediaService {

    private final MediaService mediaService;

    /**
     * Listen for media upload requests from Product Service (one-way)
     */
    @KafkaListener(
            topics = KafkaTopics.MEDIA_UPLOAD_REQUEST,
            containerFactory = "mediaUploadRequestKafkaListenerContainerFactory"
    )
    public void handleMediaUploadRequest(MediaUploadRequest request) {
        log.info("Received media upload request for ID: {}", request.getId());

        try {
            // Process the upload
            processMediaUpload(request);

            log.info("Successfully processed media upload for ID: {}", request.getId());

        } catch (Exception e) {
            log.error("Error processing media upload request for ID: {}: {}",
                    request.getId(), e.getMessage(), e);
        }
    }

    /**
     * Process media upload request (no response)
     */
    private void processMediaUpload(MediaUploadRequest request) throws Exception {
        // Decode base64 to bytes
        byte[] fileBytes = Base64.getDecoder().decode(request.getBase64Data());

        // Create a mock MultipartFile for the existing MediaService
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                request.getFileName(),
                getContentType(request.getFileName()),
                fileBytes
        );

        // Upload to Cloudinary
        Map<String, Object> uploadResult;
        if (request.getMediaType() == MediaUploadRequest.MediaType.IMAGE) {
            uploadResult = mediaService.uploadImage(mockFile, request.getFolder());
        } else {
            uploadResult = mediaService.uploadVideo(mockFile, request.getFolder());
        }

        // Extract Cloudinary URL for logging
        String cloudinaryUrl = (String) uploadResult.get("secure_url");
        log.info("Successfully uploaded media to Cloudinary: {} for request ID: {}",
                cloudinaryUrl, request.getId());
    }

    /**
     * Get content type based on file extension
     */
    private String getContentType(String fileName) {
        if (fileName == null) return "application/octet-stream";

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "mp4" -> "video/mp4";
            case "avi" -> "video/avi";
            case "mov" -> "video/quicktime";
            default -> "application/octet-stream";
        };
    }

    /**
     * Mock MultipartFile implementation for internal use
     */
    private static class MockMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public MockMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() { return name; }

        @Override
        public String getOriginalFilename() { return originalFilename; }

        @Override
        public String getContentType() { return contentType; }

        @Override
        public boolean isEmpty() { return content.length == 0; }

        @Override
        public long getSize() { return content.length; }

        @Override
        public byte[] getBytes() { return content; }

        @Override
        public java.io.InputStream getInputStream() { return new ByteArrayInputStream(content); }

        @Override
        public void transferTo(java.io.File dest) throws IOException {
            throw new UnsupportedOperationException("transferTo not supported");
        }
    }
}