package com.devwonder.warrantyservice.client;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.warrantyservice.dto.CustomerInfo;
import com.devwonder.warrantyservice.dto.CustomerDetails;
import com.devwonder.warrantyservice.dto.CheckCustomerExistsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", url = "${services.user-service.url:http://user-service:8082}")
public interface UserServiceClient {

    @GetMapping("/user-service/customers/{identifier}/check-exists")
    BaseResponse<CheckCustomerExistsResponse> checkCustomerExists(
            @PathVariable String identifier,
            @RequestHeader("X-API-Key") String apiKey
    );

    @PostMapping("/user-service/customers")
    BaseResponse<Long> createCustomer(
            @RequestBody CustomerInfo customerInfo,
            @RequestHeader("X-API-Key") String apiKey
    );

    @GetMapping("/user-service/customers/{customerId}")
    BaseResponse<String> getCustomerName(
            @PathVariable Long customerId,
            @RequestHeader("X-API-Key") String apiKey
    );

    @GetMapping("/customer/{customerId}/details")
    BaseResponse<CustomerInfo> getCustomerById(
            @PathVariable Long customerId,
            @RequestHeader("X-API-Key") String apiKey
    );
}