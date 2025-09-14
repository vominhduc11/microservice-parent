package com.devwonder.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadRequest {
    private String id; // Unique identifier for tracking
    private String base64Data; // Base64 encoded file data
    private String fileName; // Generated filename
    private String folder; // Cloudinary folder
    private MediaType mediaType; // IMAGE or VIDEO
    private String productSku; // For tracking which product this belongs to
    private String fieldPath; // E.g., "description[0].link", "videos[1].videoUrl", "images"

    public enum MediaType {
        IMAGE, VIDEO
    }
}