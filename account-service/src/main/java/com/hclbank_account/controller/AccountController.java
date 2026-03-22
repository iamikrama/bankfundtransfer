package com.hclbank_account.controller;

import com.hclbank_account.dto.*;
import com.hclbank_account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // auth.getPrincipal() returns customerUUID set by JwtAuthFilter

    // GET /accounts — slide 3
    @GetMapping
    public ResponseEntity<List<AccountSummaryResponse>>
    getAllAccounts(Authentication auth) {
        UUID customerId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(
                accountService.getAllAccounts(customerId));
    }

    // GET /accounts/{id} — slide 5 header
    @GetMapping("/{id}")
    public ResponseEntity<AccountDetailResponse>
    getAccountDetail(@PathVariable UUID id,
                     Authentication auth) {
        UUID customerId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(
                accountService.getAccountDetail(id, customerId));
    }

    // GET /accounts/{id}/statements?limit=10 — slide 5 list
    @GetMapping("/{id}/statements")
    public ResponseEntity<List<StatementResponse>>
    getStatements(@PathVariable UUID id,
                  @RequestParam(defaultValue = "10") int limit,
                  Authentication auth) {
        UUID customerId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(
                accountService.getStatements(id, customerId, limit));
    }

    // GET /accounts/{id}/statements/{txId} — drill-down
    @GetMapping("/{id}/statements/{txId}")
    public ResponseEntity<StatementDetailResponse>
    getStatementDetail(@PathVariable UUID id,
                       @PathVariable UUID txId,
                       Authentication auth) {
        UUID customerId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(
                accountService.getStatementDetail(id, txId, customerId));
    }
}


