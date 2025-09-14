package com.devwonder.productservice.constant;

public final class KafkaTopics {

    private KafkaTopics() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Topic names
    public static final String MEDIA_UPLOAD_REQUEST = "media-upload-request";
}