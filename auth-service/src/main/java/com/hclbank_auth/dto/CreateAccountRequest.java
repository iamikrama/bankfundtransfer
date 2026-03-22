package com.hclbank_auth.dto;

import lombok.*;
import java.util.UUID;

// Auth service needs its own copy of this DTO
// Feign serialises this to JSON and sends to account service
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    private UUID   customerId;
    private String accountNumber;
    private String ifscCode;
    private String name;
}