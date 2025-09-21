package com.devwonder.orderservice.client;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.orderservice.dto.DealerInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/user")
public interface UserServiceClient {

    @GetMapping("/dealers/{dealerId}?fields=companyName,email,phone,city")
    BaseResponse<DealerInfo> getDealerInfo(@PathVariable("dealerId") Long dealerId);
}