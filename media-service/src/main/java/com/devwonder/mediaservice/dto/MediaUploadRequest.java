package com.devwonder.mediaservice.dto;

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
    private String fileName; // Original filename
    private String folder; // Cloudinary folder
    private MediaType mediaType; // IMAGE or VIDEO

    public enum MediaType {
        IMAGE, VIDEO
    }
}