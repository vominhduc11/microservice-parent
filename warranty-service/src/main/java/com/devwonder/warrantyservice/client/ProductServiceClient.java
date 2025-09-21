package com.devwonder.warrantyservice.client;

import com.devwonder.common.dto.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/product/{productId}/warranty-period")
    BaseResponse<Integer> getProductWarrantyPeriod(@PathVariable Long productId);

    @GetMapping("/product-serial/serial/{serial}")
    BaseResponse<Long> getProductSerialIdBySerial(@PathVariable String serial);
}