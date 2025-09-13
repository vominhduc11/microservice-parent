package com.devwonder.mediaservice.constant;

public final class KafkaTopics {

    // Prevent instantiation
    private KafkaTopics() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Topic names
    public static final String MEDIA_UPLOAD_REQUEST = "media-upload-request";

    // Consumer groups
    public static final String MEDIA_SERVICE_UPLOAD_GROUP = "media-service-upload-group";
}