package com.hclbank_auth.client;


import com.hclbank_auth.dto.CreateAccountRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// name must match spring.application.name in account-service yml
@FeignClient(name = "account-service")
public interface AccountServiceClient {

    // Calls POST http://account-service/internal/accounts/create
    @PostMapping("/internal/accounts/create")
    void createAccount(@RequestBody CreateAccountRequest request);
}