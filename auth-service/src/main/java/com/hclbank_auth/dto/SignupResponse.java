package com.hclbank_auth.dto;

import lombok.*;

@Data @AllArgsConstructor
public class SignupResponse {
    private String customerId;  // CUST000001 — show to user
    private String token;
    private String message;
}