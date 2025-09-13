package com.devwonder.mediaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.devwonder.mediaservice", "com.devwonder.common"},
               excludeFilters = @ComponentScan.Filter(pattern = "com.devwonder.common.config.RedisConfig"))
public class MediaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediaServiceApplication.class, args);
	}

}
