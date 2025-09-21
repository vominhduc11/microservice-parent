package com.devwonder.warrantyservice.client;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.warrantyservice.dto.CustomerInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/user/customers/{identifier}/check-exists")
    BaseResponse<Long> checkCustomerExists(@PathVariable String identifier);

    @PostMapping("/customer")
    BaseResponse<Long> createCustomer(@RequestBody CustomerInfo customerInfo);

    @GetMapping("/customer/{customerId}")
    BaseResponse<String> getCustomerName(@PathVariable Long customerId);
}