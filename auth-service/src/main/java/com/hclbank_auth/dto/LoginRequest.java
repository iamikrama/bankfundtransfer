package com.hclbank_auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    @NotBlank(message = "Password is required")
    private String password;
}