package com.hclbank_account.controller;

import com.hclbank_account.dto.CreateAccountRequest;
import com.hclbank_account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

// Internal controller — NOT exposed through API Gateway
// Only Auth service calls this via Feign during signup
// SecurityConfig already has .requestMatchers("/internal/**").permitAll()
@RestController
@RequestMapping("/internal/accounts")
@RequiredArgsConstructor
public class InternalAccountController {

    private final AccountService accountService;

    // POST /internal/accounts/create
    // Called by Auth service right after a customer signs up
    @PostMapping("/create")
    public ResponseEntity<Void> createAccount(
            @RequestBody CreateAccountRequest request) {

        accountService.createAccount(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    // GET /internal/accounts/{id}/balance
    // Already exists — called by Transfer service
    @GetMapping("/{id}/balance")
    public ResponseEntity<com.hclbank_account.dto.BalanceResponse>
    getBalance(@PathVariable java.util.UUID id) {
        return ResponseEntity.ok(accountService.getBalance(id));
    }
}