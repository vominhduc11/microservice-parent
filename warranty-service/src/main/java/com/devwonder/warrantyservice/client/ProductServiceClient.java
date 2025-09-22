package com.devwonder.warrantyservice.client;

import com.devwonder.common.dto.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "product-service", url = "${services.product-service.url:http://product-service:8083}")
public interface ProductServiceClient {

    @GetMapping("/product/{productId}/warranty-period")
    BaseResponse<Integer> getProductWarrantyPeriod(
            @PathVariable Long productId,
            @RequestHeader("X-API-Key") String apiKey
    );

    @GetMapping("/product-serial/serial/{serial}")
    BaseResponse<Long> getProductSerialIdBySerial(
            @PathVariable String serial,
            @RequestHeader("X-API-Key") String apiKey
    );
}