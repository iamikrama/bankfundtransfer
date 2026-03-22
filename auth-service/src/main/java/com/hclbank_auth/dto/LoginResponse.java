package com.hclbank_auth.dto;

import lombok.*;

@Data @AllArgsConstructor
public class LoginResponse {
    private String token;
    private String customerId;
    private String fullName;
    private long   expiresIn;
}