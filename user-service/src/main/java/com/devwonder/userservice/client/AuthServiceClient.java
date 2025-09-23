package com.devwonder.userservice.client;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.userservice.dto.AuthAccountCreateRequest;
import com.devwonder.userservice.dto.AuthAccountCreateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "${services.auth-service.url:http://auth-service:8081}")
public interface AuthServiceClient {
    
    @PostMapping("/auth-service/accounts")
    BaseResponse<AuthAccountCreateResponse> createAccount(
            @RequestBody AuthAccountCreateRequest request,
            @RequestHeader("X-API-Key") String apiKey
    );

    @DeleteMapping("/auth-service/accounts/{accountId}")
    BaseResponse<String> deleteAccount(
            @PathVariable Long accountId,
            @RequestHeader("X-API-Key") String apiKey
    );
}