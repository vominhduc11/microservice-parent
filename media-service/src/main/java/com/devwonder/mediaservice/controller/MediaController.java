package com.devwonder.mediaservice.controller;

import com.devwonder.mediaservice.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/media")
@Tag(name = "Media Management", description = "Media upload and management endpoints")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload/image")
    @Operation(
        summary = "Upload Image",
        description = "Upload an image file to Cloudinary. Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or file is empty"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> uploadImage(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Optional folder name in Cloudinary")
            @RequestParam(value = "folder", required = false) String folder) {

        log.info("Received image upload request - filename: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File is empty"));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File must be an image"));
            }

            // Upload to Cloudinary
            Map<String, Object> result = mediaService.uploadImage(file, folder);

            return ResponseEntity.ok(createSuccessResponse("Image uploaded successfully", result));

        } catch (IOException e) {
            log.error("Failed to upload image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to upload image: " + e.getMessage()));
        }
    }

    @PostMapping("/upload/video")
    @Operation(
        summary = "Upload Video",
        description = "Upload a video file to Cloudinary. Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Video uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or file is empty"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @Parameter(description = "Video file to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Optional folder name in Cloudinary")
            @RequestParam(value = "folder", required = false) String folder) {

        log.info("Received video upload request - filename: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File is empty"));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File must be a video"));
            }

            // Upload to Cloudinary
            Map<String, Object> result = mediaService.uploadVideo(file, folder);

            return ResponseEntity.ok(createSuccessResponse("Video uploaded successfully", result));

        } catch (IOException e) {
            log.error("Failed to upload video: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to upload video: " + e.getMessage()));
        }
    }


    @DeleteMapping("/delete/{publicId}")
    @Operation(
        summary = "Delete Media",
        description = "Delete a media file from Cloudinary by public ID. Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Media deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> deleteMedia(
            @Parameter(description = "Public ID of the media to delete", required = true)
            @PathVariable String publicId,
            @Parameter(description = "Resource type (image, video, raw)", example = "image")
            @RequestParam(value = "resourceType", defaultValue = "image") String resourceType) {

        log.info("Received delete request - public_id: {}, resource_type: {}", publicId, resourceType);

        try {
            Map<String, Object> result = mediaService.deleteMedia(publicId, resourceType);
            return ResponseEntity.ok(createSuccessResponse("Media deleted successfully", result));

        } catch (IOException e) {
            log.error("Failed to delete media: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to delete media: " + e.getMessage()));
        }
    }

    private Map<String, Object> createSuccessResponse(String message, Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
}