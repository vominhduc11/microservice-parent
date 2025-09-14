package com.devwonder.productservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaProcessingServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MediaProcessingService mediaProcessingService;

    @Test
    void testProcessDescription_WithBase64Image() {
        // Test data
        List<Map<String, Object>> description = List.of(
                Map.of("type", "title", "text", "Product Title"),
                Map.of("type", "image", "link", "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD...")
        );

        // Mock response from media service
        Map<String, Object> mockResponse = Map.of(
                "success", true,
                "data", Map.of("secure_url", "https://cloudinary.com/uploaded-image.jpg")
        );

        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // This is a basic structure test - the actual processing would require proper ObjectMapper setup
        assertDoesNotThrow(() -> {
            Object result = mediaProcessingService.processDescription(description);
            assertNotNull(result);
        });
    }

    @Test
    void testProcessMainImage_WithBase64() {
        // Test base64 image
        String base64Image = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD...";

        // Mock response
        Map<String, Object> mockResponse = Map.of(
                "success", true,
                "data", Map.of("secure_url", "https://cloudinary.com/main-image.jpg")
        );

        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        String result = mediaProcessingService.processMainImage(base64Image);
        assertEquals("https://cloudinary.com/main-image.jpg", result);
    }

    @Test
    void testProcessMainImage_WithRegularUrl() {
        // Test regular URL (should return as-is)
        String regularUrl = "https://example.com/image.jpg";

        String result = mediaProcessingService.processMainImage(regularUrl);
        assertEquals(regularUrl, result);
    }
}