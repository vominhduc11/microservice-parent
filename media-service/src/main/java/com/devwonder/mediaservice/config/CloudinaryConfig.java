package com.devwonder.mediaservice.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "daohufjec",
                "api_key", "872317127931114",
                "api_secret", "N4qcHq42OqjlyK5GiQoZfk1UXfE",
                "secure", true));
    }
}