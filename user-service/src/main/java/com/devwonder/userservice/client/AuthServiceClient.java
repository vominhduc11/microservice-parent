package com.devwonder.userservice.client;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.userservice.dto.AuthAccountCreateRequest;
import com.devwonder.userservice.dto.AuthAccountCreateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "${services.auth-service.url:http://auth-service:8081}")
public interface AuthServiceClient {
    
    @PostMapping("/auth/accounts")
    BaseResponse<AuthAccountCreateResponse> createAccount(
            @RequestBody AuthAccountCreateRequest request,
            @RequestHeader("X-Internal-Service") String serviceIdentifier
    );
}