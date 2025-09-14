package com.devwonder.mediaservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final Cloudinary cloudinary;

    /**
     * Upload image to Cloudinary
     * @param file MultipartFile to upload
     * @param folder Optional folder name in Cloudinary
     * @return Map containing upload result with URL and public_id
     * @throws IOException if upload fails
     */
    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        log.info("Uploading image to Cloudinary - filename: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "resource_type", "image",
                "format", "auto",
                "quality", "auto"
        );

        if (folder != null && !folder.trim().isEmpty()) {
            uploadParams.put("folder", folder);
        }

        try {
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), uploadParams);
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
     * @param file MultipartFile to upload
     * @param folder Optional folder name in Cloudinary
     * @return Map containing upload result with URL and public_id
     * @throws IOException if upload fails
     */
    public Map<String, Object> uploadVideo(MultipartFile file, String folder) throws IOException {
        log.info("Uploading video to Cloudinary - filename: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "resource_type", "video",
                "format", "auto",
                "quality", "auto"
        );

        if (folder != null && !folder.trim().isEmpty()) {
            uploadParams.put("folder", folder);
        }

        try {
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            log.info("Successfully uploaded video to Cloudinary - public_id: {}, url: {}",
                    result.get("public_id"), result.get("secure_url"));
            return result;
        } catch (IOException e) {
            log.error("Failed to upload video to Cloudinary: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Upload base64 encoded image to Cloudinary
     * @param base64Data Base64 encoded image data
     * @param fileName File name for the upload
     * @param folder Optional folder name in Cloudinary
     * @return Map containing upload result with URL and public_id
     * @throws IOException if upload fails
     * @throws IllegalArgumentException if base64 data is invalid
     */
    public Map<String, Object> uploadBase64Image(String base64Data, String fileName, String folder) throws IOException {
        log.info("Uploading base64 image to Cloudinary - filename: {}", fileName);

        try {
            // Decode base64 data
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "resource_type", "image",
                    "format", "auto",
                    "quality", "auto",
                    "public_id", fileName.replaceAll("\\.[^.]*$", "") // Remove extension from filename for public_id
            );

            if (folder != null && !folder.trim().isEmpty()) {
                uploadParams.put("folder", folder);
            }

            Map<String, Object> result = cloudinary.uploader().upload(imageBytes, uploadParams);
            log.info("Successfully uploaded base64 image to Cloudinary - public_id: {}, url: {}",
                    result.get("public_id"), result.get("secure_url"));
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Invalid base64 data: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("Failed to upload base64 image to Cloudinary: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Delete media from Cloudinary
     * @param publicId Public ID of the media to delete
     * @param resourceType Resource type (image, video, raw)
     * @return Map containing deletion result
     * @throws IOException if deletion fails
     */
    public Map<String, Object> deleteMedia(String publicId, String resourceType) throws IOException {
        log.info("Deleting media from Cloudinary - public_id: {}, resource_type: {}", publicId, resourceType);

        try {
            Map<String, Object> result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType));
            log.info("Successfully deleted media from Cloudinary - public_id: {}, result: {}",
                    publicId, result.get("result"));
            return result;
        } catch (IOException e) {
            log.error("Failed to delete media from Cloudinary: {}", e.getMessage(), e);
            throw e;
        }
    }
}