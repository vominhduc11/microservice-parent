package com.devwonder.mediaservice.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.mediaservice.service.MediaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/media")
@Tag(name = "Media Management", description = "Media upload and management endpoints")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload")
    @Operation(summary = "Upload Image", description = "Upload image file to Cloudinary. Only images are supported. Requires ADMIN role authentication via API Gateway.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file, empty file, or unsupported file type"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<Map<String, Object>>> uploadImage(
            @Parameter(description = "Image file to upload", required = true) @RequestParam("file") MultipartFile file) {

        log.info("Received image upload request - filename: {}, size: {} bytes, type: {}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.error("File is empty"));
            }

            String contentType = file.getContentType();
            if (contentType == null) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.error("Cannot determine file type"));
            }

            if (!contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.error("File must be an image"));
            }

            Map<String, Object> result = mediaService.uploadImage(file);
            return ResponseEntity.ok(BaseResponse.success("Image uploaded successfully to Cloudinary", result));

        } catch (IOException e) {
            log.error("Failed to upload image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Failed to upload image: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete Image", description = "Delete an image from Cloudinary by public ID. Requires ADMIN role authentication via API Gateway.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<Map<String, Object>>> deleteImage(
            @RequestParam("publicId") String publicId) {

        log.info("Received delete request - public_id: {}", publicId);

        try {
            Map<String, Object> result = mediaService.deleteImage(publicId);
            return ResponseEntity.ok(BaseResponse.success("Image deleted successfully from Cloudinary", result));

        } catch (IOException e) {
            log.error("Failed to delete image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Failed to delete image: " + e.getMessage()));
        }
    }


}