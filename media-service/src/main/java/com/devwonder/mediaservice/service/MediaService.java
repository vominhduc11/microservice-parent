package com.devwonder.mediaservice.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final Cloudinary cloudinary;

    /**
     * Upload image to Cloudinary
     * 
     * @param file   MultipartFile to upload
     * @param folder Optional folder name in Cloudinary
     * @return Map containing upload result with URL and public_id
     * @throws IOException if upload fails
     */
    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        log.info("Uploading image to Cloudinary - filename: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "image",
                    "folder", folder != null && !folder.trim().isEmpty() ? folder : "images"
            ));

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) uploadResult;

            log.info("Successfully uploaded image to Cloudinary - public_id: {}, url: {}",
                    result.get("public_id"), result.get("secure_url"));
            return result;
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Upload video to Cloudinary
     * 
     * @param file   MultipartFile to upload
     * @param folder Optional folder name in Cloudinary
     * @return Map containing upload result with URL and public_id
     * @throws IOException if upload fails
     */
    public Map<String, Object> uploadVideo(MultipartFile file, String folder) throws IOException {
        log.info("Uploading video to Cloudinary - filename: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "video",
                    "folder", folder != null && !folder.trim().isEmpty() ? folder : "videos_short"
            ));

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) uploadResult;

            log.info("Successfully uploaded video to Cloudinary - public_id: {}, url: {}",
                    result.get("public_id"), result.get("secure_url"));
            return result;
        } catch (IOException e) {
            log.error("Failed to upload video to Cloudinary: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Delete media from Cloudinary
     * 
     * @param publicId     Public ID of the media to delete
     * @param resourceType Resource type (image, video, raw)
     * @return Map containing deletion result
     * @throws IOException if deletion fails
     */
    public Map<String, Object> deleteMedia(String publicId, String resourceType) throws IOException {
        log.info("Deleting media from Cloudinary - public_id: {}, resource_type: {}", publicId, resourceType);

        try {
            Map<?, ?> deleteResult = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType));

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) deleteResult;

            log.info("Successfully deleted media from Cloudinary - public_id: {}, result: {}",
                    publicId, result.get("result"));
            return result;
        } catch (IOException e) {
            log.error("Failed to delete media from Cloudinary: {}", e.getMessage(), e);
            throw e;
        }
    }
}