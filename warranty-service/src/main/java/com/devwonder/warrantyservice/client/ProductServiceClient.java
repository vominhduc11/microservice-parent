package com.devwonder.warrantyservice.client;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.warrantyservice.dto.ProductSerialBulkStatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service", url = "${services.product-service.url:http://product-service:8083}")
public interface ProductServiceClient {

    @GetMapping("/product-serial/serial/{serial}")
    BaseResponse<Long> getProductSerialIdBySerial(
            @PathVariable String serial,
            @RequestHeader("X-API-Key") String apiKey
    );

    @PatchMapping("/product-serial/bulk-status")
    BaseResponse<String> updateProductSerialsToSoldToCustomer(
            @RequestBody ProductSerialBulkStatusUpdateRequest request,
            @RequestHeader("X-API-Key") String apiKey
    );
}